package server.veo.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageException;
import com.google.cloud.storage.StorageOptions;
import com.google.genai.Client;
import com.google.genai.types.GenerateVideosConfig;
import com.google.genai.types.GenerateVideosOperation;
import com.google.genai.types.Image;
import com.google.genai.types.Video;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.veo.dto.GeneratedVideo;
import server.veo.dto.VideoGenerationRequest;
import server.veo.dto.VideoGenerationResponse;

/**
 * 해커톤용 간단한 동기식 Veo 비디오 생성 서비스
 * 복잡한 비동기, Redis, SSE 기능 없이 가장 단순하게 구현
 *
 * ✅ 클라이언트에서 직접 업로드한 이미지 파일을 GCS에 저장 후 비디오 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class VeoVideoService {

    private final Client genAiClient;
    private final GcsImageUploadService gcsImageUploadService;
    // ✅ VideoStorageService 제거 - storageUri 사용으로 불필요
    
    @Value("${google.cloud.storage.bucket}")
    private String gcsBucketName;
    
    // ✅ 안정적인 Veo 2.0 모델 사용 (404 오류 방지)
    private static final String VEO_MODEL = "veo-3.0-generate-preview"; // ✅ Veo 3.0
    // 🔄 Fallback 모델 옵션들:
    // - "veo-3.0-generate-preview" (Preview, us-central1/us-east5만 지원)
    // - "veo-3.0-generate-001" (최신 GA, 제한적 지역 지원)

    private static final int MAX_WAIT_SECONDS = 300; // 5분 최대 대기

    /**
     * 동기식 비디오 생성 - 완료될 때까지 기다렸다가 결과 반환
     * ✅ 클라이언트 이미지 파일 → GCS 업로드 → 비디오 생성
     */
    public VideoGenerationResponse generate(VideoGenerationRequest request) {
        String operationId = generateOperationId();
        String prompt = "음식점 홍보 숏폼 만들어줘";
        log.info("🎬 비디오 생성 시작 - 작업 ID: {}, 프롬프트: {}, 이미지 개수: {}",
                operationId, prompt,
                request.imageFiles() != null ? request.imageFiles().length : 0);

        // ✅ 1. 이미지 파일을 GCS에 업로드
        List<String> gcsImageUris = uploadImagesToGcs(request.imageFiles());
        log.info("🔄 이미지 GCS 업로드 완료 - 업로드된 개수: {}", gcsImageUris.size());

        // 2. 설정 준비 - ✅ GCS에 직접 저장하도록 storageUri 설정
        String storageUri = String.format("gs://%s/videos/", gcsBucketName);
        GenerateVideosConfig config = GenerateVideosConfig.builder()
                .aspectRatio("16:9")
                .seed(8)
                .generateAudio(true)
                .outputGcsUri(storageUri)  // ✅ Veo가 직접 GCS에 저장
                .build();

        log.info("🗄️ Veo 비디오 저장 위치: {}", storageUri);

        // 3. 이미지 처리 및 비디오 생성
        GenerateVideosOperation operation;

        log.info("🖼️ 이미지 포함 비디오 생성 - GCS 이미지 개수: {}", gcsImageUris.size());

        // ✅ 첫 번째 이미지만 사용 (SDK가 contextImages를 지원하지 않음)
        Image primaryImage = Image.builder()
                .gcsUri(gcsImageUris.get(0))
                .mimeType("image/jpeg")  // 기본 MIME 타입 설정
                .build();

        String enhancedPrompt = enhancePromptWithMultipleImages(prompt, gcsImageUris);
        log.info("✅ 첫 번째 이미지를 기본으로 사용, 나머지 {}개 이미지는 프롬프트에 반영됨",
                gcsImageUris.size() - 1);

        operation = genAiClient.models.generateVideos(
                VEO_MODEL, enhancedPrompt, primaryImage, config);

        // 4. 완료 대기
        operation = waitForCompletion(operation, operationId);

        // 5. 결과 처리
        GeneratedVideo video = processResults(operation, request);

        log.info("✅ 비디오 생성 완료 - 작업 ID: {}, 생성된 비디오: {}",
                operationId, video != null ? "성공" : "실패");

        return VideoGenerationResponse.completed(operationId, prompt, video);
    }

    /**
     * 이미지 파일들을 GCS에 업로드
     */
    private List<String> uploadImagesToGcs(MultipartFile[] imageFiles) {
        if (imageFiles == null || imageFiles.length == 0) {
            log.info("📷 업로드할 이미지 파일이 없음");
            return List.of();
        }

        // 유효한 이미지 파일만 필터링
        List<MultipartFile> validImageFiles = new ArrayList<>();
        for (MultipartFile file : imageFiles) {
            if (gcsImageUploadService.isValidImageFile(file)) {
                validImageFiles.add(file);
                log.info("✅ 유효한 이미지 파일: {} ({})",
                        file.getOriginalFilename(), file.getContentType());
            } else {
                log.warn("⚠️ 유효하지 않은 이미지 파일 스킵: {}",
                        file != null ? file.getOriginalFilename() : "null");
            }
        }

        if (validImageFiles.isEmpty()) {
            log.warn("📷 유효한 이미지 파일이 없음");
            return List.of();
        }

        // GCS에 업로드
        return gcsImageUploadService.uploadImages(validImageFiles.toArray(new MultipartFile[0]));
    }

    /**
     * 이미지를 고려한 프롬프트 향상 (여러 이미지 지원)
     */
    private String enhancePromptWithMultipleImages(String originalPrompt, List<String> gcsImageUris) {
        if (gcsImageUris.size() == 0) {
            return originalPrompt;
        }

        StringBuilder enhancedPrompt = new StringBuilder(originalPrompt);

        if (gcsImageUris.size() == 1) {
            enhancedPrompt.append(". 제공된 이미지를 참고하여 음식점의 분위기와 음식을 잘 표현해주세요.");
        } else {
            enhancedPrompt.append(". 총 ").append(gcsImageUris.size()).append("개의 이미지를 참고하여 음식점의 분위기와 음식을 잘 표현해주세요.");
            enhancedPrompt.append(" 첫 번째 이미지는 주요 시작 장면으로 사용되며, 나머지 이미지들은 음식점의 다양한 각도와 분위기를 나타냅니다.");
            enhancedPrompt.append(" 이미지들 간의 자연스러운 연결과 일관된 스토리라인을 만들어주세요.");
        }

        return enhancedPrompt.toString();
    }

    /**
     * 작업 완료 대기
     */
    private GenerateVideosOperation waitForCompletion(GenerateVideosOperation operation, String operationId) {
        int waitCount = 0;
        int maxWaits = MAX_WAIT_SECONDS / 5; // 5초마다 체크

        while (!operation.done().orElse(false) && waitCount < maxWaits) {
            waitCount++;
            int progressPercent = Math.min((waitCount * 100) / maxWaits, 99);

            log.info("⏳ 비디오 생성 대기 중... 작업 ID: {}, 진행률: {}% ({}/{})",
                    operationId, progressPercent, waitCount, maxWaits);

            try {
                Thread.sleep(5000); // 5초 대기
                operation = genAiClient.operations.getVideosOperation(operation, null);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("비디오 생성 대기 중 인터럽트 발생", e);
            }
        }

        if (!operation.done().orElse(false)) {
            throw new RuntimeException("비디오 생성 시간 초과 (5분)");
        }

        log.info("🎉 비디오 생성 작업 완료 - 작업 ID: {}", operationId);
        return operation;
    }

    /**
     * 생성 결과 처리 - Google 원본 응답 확인
     */
    private GeneratedVideo processResults(GenerateVideosOperation operation, VideoGenerationRequest request) {
        log.info("🔍 결과 처리 시작 - operation 상태 확인");
        log.info("📊 Operation 전체 정보: {}", operation.toString());
        log.info("📊 Operation done: {}", operation.done().isPresent() ? operation.done().get() : "Empty");
        log.info("📊 Operation error: {}", operation.error().isPresent() ? operation.error().get() : "No error");
        log.info("📊 Operation response present: {}", operation.response().isPresent());

        // 1. operation.response() 확인
        var responseOpt = operation.response();
        if (responseOpt.isEmpty()) {
            log.warn("❌ operation.response()가 비어있습니다");
            return null; // 비디오 생성 실패 처리
        }

        log.info("✅ operation.response() 존재함");
        var response = responseOpt.get();
        log.info("📊 Response 전체 정보: {}", response.toString());

        // 2. response.generatedVideos() 확인
        var generatedVideosOpt = response.generatedVideos();
        if (generatedVideosOpt.isEmpty()) {
            log.warn("❌ response.generatedVideos()가 비어있습니다");
            return null; // 비디오 생성 실패 처리
        }

        log.info("✅ response.generatedVideos() 존재함");
        var generatedVideos = generatedVideosOpt.get();
        log.info("📊 GeneratedVideos 크기: {}", generatedVideos.size());

        // 3. 각 비디오 처리
        // ✅ 첫 번째 비디오만 반환
        var videoWrapper = generatedVideos.get(0);
        log.info("📊 VideoWrapper[{}] 정보: {}", 0, videoWrapper.toString());

        var videoOpt = videoWrapper.video();
        if (videoOpt.isEmpty()) {
            log.warn("❌ VideoWrapper[0].video()가 비어있습니다");
            return null; // 비디오 생성 실패 처리
        }

        Video video = videoOpt.get();
        log.info("📊 Video[{}] 정보: {}", 0, video.toString());

        // ✅ storageUri 설정으로 인해 URI 방식으로만 반환됨
        var uriOpt = video.uri();
        if (uriOpt.isEmpty() || uriOpt.get().isEmpty()) {
            log.warn("❌ Video[0].uri()가 비어있습니다 - storageUri 설정 확인 필요");
            return null; // 비디오 생성 실패 처리
        }
        
        String videoUrl = uriOpt.get();
        videoUrl.replace("gs://", "https://storage.cloud.google.com/");

        log.info("✅ Video[{}] GCS URI: {}", 0, videoUrl);

        GeneratedVideo generatedVideo = GeneratedVideo.create(
                "temp",
                videoUrl,
                8
        );

        log.info("📹 처리된 비디오 수: {}", 1);
        return generatedVideo;
    }

    /**
     * 유니크한 작업 ID 생성
     */
    private String generateOperationId() {
        return "sync_veo_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8);
    }
} 
