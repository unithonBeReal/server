package server.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChallengeRequest {

    private Long bookId;

    private int totalPages;

    @Min(value = 1, message = "시작 페이지는 1 이상이어야 합니다.")
    @Schema(description = "독서 시작 페이지", defaultValue = "1")
    private int startPage;

    @Min(value = 1, message = "끝 페이지는 1 이상이어야 합니다.")
    @Schema(description = "독서 끝 페이지")
    private int endPage;

    @AssertTrue(message = "시작 페이지는 끝 페이지보다 클 수 없습니다.")
    private boolean isStartPageValid() {
        return startPage <= endPage;
    }
}
