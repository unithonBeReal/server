package server.veo.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import server.common.response.ResponseForm;
import server.veo.dto.VideoGenerationRequest;
import server.veo.dto.VideoGenerationResponse;
import server.veo.service.VeoVideoService;

/**
 * 해커톤용 간단한 동기식 Veo 비디오 생성 API
 * 복잡한 비동기, SSE, WebSocket 기능 없이 가장 단순하게 구현
 * ✅ multipart/form-data로 이미지 파일 직접 업로드 지원
 */
@RestController
@RequestMapping("/api/v1/veo")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Veo Video Generation", description = "간단한 동기식 비디오 생성 API (해커톤용)")
public class VeoController {

    private final VeoVideoService veoVideoService;

    /**
     * 동기식 비디오 생성 - 완료될 때까지 기다렸다가 결과 반환
     * ✅ 이미지 파일을 직접 업로드하여 비디오 생성
     */
    @PostMapping(value = "/generate", consumes = "multipart/form-data")
    @Operation(
            summary = "동기식 비디오 생성 (이미지 파일 업로드)",
            description = "프롬프트와 이미지 파일들을 입력받아 동기적으로 비디오를 생성하고 결과를 반환합니다. (완료까지 약 2-3분 소요)"
    )
    public ResponseForm<VideoGenerationResponse> generateVideo(
            @Parameter(description = "이미지 파일들 (최대 10개)", required = false)
            @RequestPart(value = "imageFiles", required = false)
            MultipartFile[] imageFiles) {

        log.info("🎬 비디오 생성 요청");

        // VideoGenerationRequest 객체 생성
        VideoGenerationRequest request = new VideoGenerationRequest(
                imageFiles
        );

        // 동기적으로 비디오 생성 (완료까지 기다림)
        VideoGenerationResponse response = veoVideoService.generate(request);

        return new ResponseForm<>(response);

    }

    /**
     * 간단한 상태 확인 엔드포인트
     */
    @GetMapping("/health")
    @Operation(summary = "서비스 상태 확인", description = "Veo 서비스의 상태를 확인합니다")
    public ResponseForm<String> healthCheck() {
        return new ResponseForm<>("Veo Service is running!");
    }
} 
