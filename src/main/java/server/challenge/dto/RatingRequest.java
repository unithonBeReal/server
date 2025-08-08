package server.challenge.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class RatingRequest {

    @NotNull
    @Min(1)
    @Max(5)
    private Integer rating;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer recommendationScore;
}
