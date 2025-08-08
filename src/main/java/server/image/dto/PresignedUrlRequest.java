package server.image.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedUrlRequest {

    @NotBlank(message = "파일 이름이 없습니다.")
    private String fileName;
} 
