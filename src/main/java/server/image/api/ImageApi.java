package server.image.api;

import server.common.response.ResponseForm;
import server.image.AwsS3ImageService;
import server.image.ImageCategory;
import server.image.dto.S3PresignedUrl;
import server.image.dto.PresignedUrlRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "image", description = "이미지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/images")
public class ImageApi {

    private final AwsS3ImageService awsS3ImageService;

    @Operation(summary = "이미지 저장은 presignedURL 방식을 이용한다.")
    @PostMapping("/presigned-url/{imageCategory}")
    public ResponseForm<S3PresignedUrl> getPresignedUrl(
            @PathVariable ImageCategory imageCategory,
            @RequestBody @Valid PresignedUrlRequest request
    ) {
        return new ResponseForm<>(awsS3ImageService.getPreSignedUrl(request.getFileName(), imageCategory));
    }
}
