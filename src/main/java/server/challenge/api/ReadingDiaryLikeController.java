package server.challenge.api;

import server.challenge.dto.response.DiaryResponse;
import server.challenge.service.ReadingDiaryLikeService;
import server.common.response.CursorPageResponse;
import server.common.response.ResponseForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "reading diary like", description = "독서 일지 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/reading-diaries")
public class ReadingDiaryLikeController {

    private final ReadingDiaryLikeService readingDiaryLikeService;

    @Operation(summary = "내가 좋아요한 독서 일기 썸네일 목록 조회")
    @GetMapping("/likes/thumbnail")
    public ResponseForm<CursorPageResponse<DiaryResponse.LikedDiaryThumbnail>> findLikedDiaryThumbnails(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "마지막으로 조회된 좋아요 ID, 첫 조회 시에는 미포함")
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(readingDiaryLikeService.findLikedDiaryThumbnails(memberId, cursorId, size));
    }

    @Operation(summary = "내가 좋아요한 독서 일기 피드 목록 조회")
    @GetMapping("/likes/feed")
    public ResponseForm<CursorPageResponse<DiaryResponse.LikedDiaryFeed>> findLikedDiaryFeeds(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "마지막으로 조회된 좋아요 ID, 첫 조회 시에는 미포함")
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(readingDiaryLikeService.findLikedDiaryFeeds(memberId, cursorId, size));
    }

    @Operation(summary = "독서일지 좋아요")
    @PostMapping("/{diaryId}/like")
    public ResponseForm<Void> likeDiary(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId
    ) {
        readingDiaryLikeService.createDiaryLike(memberId, diaryId);
        return ResponseForm.ok();
    }

    @Operation(summary = "독서일지 좋아요 취소")
    @DeleteMapping("/{diaryId}/like")
    public ResponseForm<Void> unlikeDiary(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId
    ) {
        readingDiaryLikeService.deleteDiaryLike(memberId, diaryId);
        return ResponseForm.ok();
    }
} 
