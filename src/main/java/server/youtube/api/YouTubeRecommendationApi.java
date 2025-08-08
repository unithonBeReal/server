package server.youtube.api;

import server.common.response.ResponseForm;
import server.youtube.dto.RecommendedVideoResponse;
import server.youtube.service.YouTubeRecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "youtube", description = "유튜브 추천 관련 API")
@RestController
@RequestMapping("/api/v2/youtube")
@RequiredArgsConstructor
public class YouTubeRecommendationApi {

    private final YouTubeRecommendationService youTubeRecommendationService;

    @Operation(summary = "도서 관련 유튜브 영상 추천", description = "관련 유튜브 영상을 10개를 추천합니다.")
    @GetMapping("/recommend")
    public ResponseForm<List<RecommendedVideoResponse>> recommendVideos(
            @AuthenticationPrincipal Long memberId
    ) {
        List<RecommendedVideoResponse> response = youTubeRecommendationService.recommendVideos(memberId);
        return new ResponseForm<>(response);
    }

    @Operation(summary = "사용자가 시청한 유튜브 영상 기록 수집")
    @PostMapping("/watch/{videoId}")
    public ResponseForm<Void> recordWatchHistory(
            @AuthenticationPrincipal Long memberId,
            @PathVariable String videoId
    ) {
        youTubeRecommendationService.recordWatchHistory(memberId, videoId);
        return new ResponseForm<>();
    }
} 
