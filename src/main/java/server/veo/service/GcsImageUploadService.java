package server.veo.service;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Google Cloud Storage 이미지 업로드 서비스
 * MultipartFile을 GCS에 업로드하고 GCS URI 반환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GcsImageUploadService {

    private final Storage gcsStorage;

    @Value("${google.cloud.storage.bucket}")
    private String bucketName;

    /**
     * 여러 이미지 파일을 GCS에 업로드하고 GCS URI 목록 반환
     */
    public List<String> uploadImages(MultipartFile[] imageFiles) {
        if (imageFiles == null || imageFiles.length == 0) {
            log.info("📷 업로드할 이미지 파일이 없습니다");
            return List.of();
        }

        List<String> gcsUris = new ArrayList<>();
        
        for (int i = 0; i < imageFiles.length; i++) {
            MultipartFile imageFile = imageFiles[i];
            
            if (imageFile == null || imageFile.isEmpty()) {
                log.warn("⚠️ 빈 이미지 파일 스킵 - 인덱스: {}", i);
                continue;
            }
            
            try {
                String gcsUri = uploadSingleImage(imageFile, i);
                gcsUris.add(gcsUri);
                log.info("✅ 이미지 업로드 성공 - 파일명: {}, GCS URI: {}", 
                        imageFile.getOriginalFilename(), gcsUri);
                
            } catch (Exception e) {
                log.error("❌ 이미지 업로드 실패 - 파일명: {}, 오류: {}", 
                        imageFile.getOriginalFilename(), e.getMessage(), e);
                // 하나 실패해도 다른 이미지들은 계속 처리
            }
        }

        log.info("📷 전체 이미지 업로드 완료 - 성공: {}/{}", gcsUris.size(), imageFiles.length);
        return gcsUris;
    }

    /**
     * 단일 이미지 파일을 GCS에 업로드
     */
    private String uploadSingleImage(MultipartFile imageFile, int index) throws IOException {
        // 고유한 파일명 생성
        String originalFilename = imageFile.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uniqueFilename = String.format("veo_image_%s_%d_%s%s", 
                timestamp, index, UUID.randomUUID().toString().substring(0, 8), fileExtension);

        // GCS 경로 설정
        String gcsPath = "veo-images/" + uniqueFilename;

        try {
            // BlobId 및 BlobInfo 생성
            BlobId blobId = BlobId.of(bucketName, gcsPath);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(imageFile.getContentType())
                    .build();

            // GCS에 업로드
            gcsStorage.create(blobInfo, imageFile.getBytes());
            
            // GCS URI 반환
            String gcsUri = String.format("gs://%s/%s", bucketName, gcsPath);
            
            log.debug("🔗 GCS 업로드 완료 - 원본: {}, GCS 경로: {}", originalFilename, gcsUri);
            return gcsUri;
            
        } catch (IOException e) {
            log.error("❌ GCS 업로드 실패 - 파일: {}, 경로: {}, 오류: {}", 
                    originalFilename, gcsPath, e.getMessage());
            throw e;
        }
    }

    /**
     * 파일 확장자 추출
     */
    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".jpg"; // 기본 확장자
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        String extension = filename.substring(lastDotIndex);
        
        // 지원되는 이미지 확장자 확인
        List<String> supportedExtensions = List.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp");
        if (supportedExtensions.contains(extension.toLowerCase())) {
            return extension.toLowerCase();
        }
        
        return ".jpg"; // 지원되지 않는 확장자인 경우 기본값
    }

    /**
     * 이미지 파일 유효성 검증
     */
    public boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }

        // 지원되는 이미지 MIME 타입
        List<String> supportedTypes = List.of(
                "image/jpeg", "image/jpg", "image/png", 
                "image/gif", "image/bmp", "image/webp"
        );

        return supportedTypes.contains(contentType.toLowerCase());
    }
} 