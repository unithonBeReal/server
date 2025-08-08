package server.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageRequest {

    @Schema(description = "이미지 URL")
    private String imageUrl;

    @Schema(description = "이미지 순서")
    private int sequence;
} 
