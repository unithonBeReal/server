package server.challenge.api;

import server.challenge.dto.response.DiaryResponse;
import server.challenge.service.ReadingDiaryScrapService;
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

@Tag(name = "reading diary scrap", description = "독서 일기 스크랩 API")
@RestController
@RequestMapping("/api/v2/scraps/reading-diaries")
@RequiredArgsConstructor
public class ReadingDiaryScrapController {

    private final ReadingDiaryScrapService readingDiaryScrapService;

    @Operation(summary = "내가 스크랩한 독서 일기 <썸네일> 목록 조회")
    @GetMapping("/thumbnail")
    public ResponseForm<CursorPageResponse<DiaryResponse.ScrapedDiaryThumbnail>> findScrapedDiaryThumbnails(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "마지막으로 조회된 스크랩 ID, 첫 조회 시에는 미포함")
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(readingDiaryScrapService.findScrapedDiaryThumbnails(memberId, cursorId, size));
    }

    @Operation(summary = "내가 스크랩한 독서 일기 <피드> 목록 조회")
    @GetMapping("/feed")
    public ResponseForm<CursorPageResponse<DiaryResponse.ScrapedDiaryFeed>> findScrapedDiaryFeeds(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "마지막으로 조회된 스크랩 ID, 첫 조회 시에는 미포함")
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(readingDiaryScrapService.findScrapedDiaryFeeds(memberId, cursorId, size));
    }

    @Operation(summary = "독서 일지 스크랩")
    @PostMapping("/{diaryId}")
    public ResponseForm<Void> createScrap(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId) {
        readingDiaryScrapService.createScrap(memberId, diaryId);
        return ResponseForm.ok();
    }

    @Operation(summary = "독서 일지 스크랩 취소")
    @DeleteMapping("/{diaryId}")
    public ResponseForm<Void> deleteScrap(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId) {
        readingDiaryScrapService.deleteScrap(memberId, diaryId);
        return ResponseForm.ok();
    }
} 
