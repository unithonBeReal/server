package server.veo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

/**
 * 비디오 생성 요청 DTO
 * 
 * 사용자가 음식점 동영상 생성을 위해 제공하는 프롬프트와 이미지 파일들을 담는 클래스입니다.
 * ✅ 해커톤용으로 비디오는 항상 1개만 생성됩니다.
 * ✅ 클라이언트에서 이미지 파일을 직접 업로드하여 GCS에 저장 후 비디오 생성
 */
public record VideoGenerationRequest(
        @Size(max = 10, message = "이미지는 최대 10개까지 가능합니다")
        MultipartFile[] imageFiles // ✅ 클라이언트에서 직접 업로드하는 이미지 파일들
) {
} 
