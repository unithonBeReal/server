package server.challenge.api;

import server.challenge.dto.DiaryRequest;
import server.challenge.dto.DiaryUpdateRequest;
import server.challenge.dto.response.DiaryResponse;
import server.challenge.dto.response.DiaryResponse.RelatedDiaryThumbnailByBook;
import server.challenge.service.ReadingDiaryService;
import server.common.response.CursorPageResponse;
import server.common.response.DualCursorPageResponse;
import server.common.response.RankedPageResponse;
import server.common.response.ResponseForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "reading diary", description = "독서 일지 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class ReadingDiaryController {

    private final ReadingDiaryService readingDiaryService;

    @Operation(summary = "특정 사용자의 전체 독서 일기 <썸네일> 목록 조회(최신순)")
    @GetMapping("/reading-diaries/members/{memberId}/thumbnail")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryThumbnail>> getMemberDiaries(
            @PathVariable Long memberId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(readingDiaryService.getDiariesThumbnailByMember(memberId, cursorId, size));
    }

    @Operation(summary = "특정 사용자의 전체 독서 일기 피드 목록 조회 (최신순)", description = "커서 기반 페이지네이션을 사용합니다. 마지막으로 본 다이어리의 ID를 cursor로 전달하세요.")
    @GetMapping("/reading-diaries/members/{memberId}/feed")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryFeed>> getLatestDiariesFeedByMember(
            @AuthenticationPrincipal Long requesterId,
            @PathVariable Long memberId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(
                readingDiaryService.getLatestDiariesFeedByMember(requesterId, memberId, cursor, size));
    }


    @Operation(summary = "나의 팔로잉 독서 일기 피드 목록 조회 (최신순)", description = "팔로잉하는 사용자들의 독서일지를 최신순으로 조회합니다. 커서 기반 페이지네이션을 사용합니다.")
    @GetMapping("/reading-diaries/following/feed")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryFeed>> getFollowingDiariesFeed(
            @AuthenticationPrincipal Long requesterId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(
                readingDiaryService.getFollowingDiariesFeed(requesterId, cursor, size));
    }

    @Operation(summary = "책별 모두의 독서 일지 <썸네일> 목록 조회 (최신순)")
    @GetMapping("books/{bookId}/reading-diaries/thumbnail")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryThumbnail>> getRelatedLatestDiaryThumbnailsByBook(
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(
                readingDiaryService.getRelatedLatestDiaryThumbnailsByBook(bookId, cursorId, size)
        );
    }

    @Operation(summary = "책별 모두의 독서 일지 <썸네일> 목록 조회 (인기순)")
    @GetMapping("books/{bookId}/reading-diaries/thumbnail/popular")
    public ResponseForm<DualCursorPageResponse<RelatedDiaryThumbnailByBook, Double>> getRelatedPopularDiaryThumbnailsByBook(
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursorId,
            @Parameter(description = "인기순 정렬 시 다음 페이지 조회를 위한 인기스코어")
            @RequestParam(required = false, defaultValue = "999999999.0") Double cursorScore,
            @RequestParam(defaultValue = "18") int size
    ) {
        return new ResponseForm<>(
                readingDiaryService.getRelatedPopularDiaryThumbnailsByBook(bookId, cursorId, cursorScore, size)
        );
    }

    @Operation(summary = "책별 모두의 독서 일지 <피드> 목록 조회 (최신순)", description = "커서 기반 페이지네이션")
    @GetMapping("books/{bookId}/reading-diaries/feed")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryFeed>> getLatestDiaryFeedsByBook(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size
    ) {
        return new ResponseForm<>(
                readingDiaryService.getLatestDiaryFeedsByBook(memberId, bookId, cursorId, size));
    }

    @Operation(summary = "책별 모두의 독서 일지 <피드> 목록 조회 (인기순)", description = "커서 기반 페이지네이션")
    @GetMapping("books/{bookId}/reading-diaries/feed/popular")
    public ResponseForm<DualCursorPageResponse<DiaryResponse.RelatedDiaryFeedByBook, Double>> getPopularDiaryFeedsByBook(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursorId,
            @Parameter(description = "인기순 정렬 시 다음 페이지 조회를 위한 인기스코어")
            @RequestParam(required = false, defaultValue = "999999999.0") Double cursorSore,
            @RequestParam(defaultValue = "18") int size
    ) {
        return new ResponseForm<>(
                readingDiaryService.getPopularDiaryFeedsByBook(memberId, bookId, cursorId, cursorSore, size));
    }


    @Operation(summary = "챡별 나의 독서 일지 <썸네일> 목록 조회 (최신순)", description = "커서 기반 페이지네이션")
    @GetMapping("books/{bookId}/my-reading-diaries/thumbnail")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryThumbnail>> getLatestDiaryThumbnailsByChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(
                readingDiaryService.getLatestDiaryThumbnailsByMemberAndBook(memberId, bookId, cursorId, size));
    }

    @Operation(summary = "책별 나의 독서 일지 <썸네일> 목록 조회 (인기순)", description = "페이지 번호 기반 페이지네이션")
    @GetMapping("books/{bookId}/my-reading-diaries/thumbnail/popular")
    public ResponseForm<RankedPageResponse<DiaryResponse.DiaryThumbnail>> getPopularDiaryThumbnailsByChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "18") int size) {
        return new ResponseForm<>(
                readingDiaryService.getPopularDiaryThumbnailsByMemberAndBook(memberId, bookId, page, size));
    }

    @Operation(summary = "책별 나의 독서 일지 <피드> 목록 조회 (최신순)", description = "커서 기반 페이지네이션")
    @GetMapping("books/{bookId}/my-reading-diaries/feed")
    public ResponseForm<CursorPageResponse<DiaryResponse.DiaryFeed>> getLatestDiaryFeedsByChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "18") int size
    ) {
        return new ResponseForm<>(
                readingDiaryService.getLatestDiaryFeedsByMemberAndBook(memberId, bookId, cursorId, size));
    }

    @Operation(summary = "책별 나의 독서 일지 <피드> 목록 조회 (인기순)", description = "페이지 번호 기반 페이지네이션")
    @GetMapping("books/{bookId}/my-reading-diaries/feed/popular")
    public ResponseForm<RankedPageResponse<DiaryResponse.DiaryFeed>> getPopularDiaryFeedsByChallenge(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long bookId,
            @Parameter(description = "페이지 번호 (0부터 시작)")
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "18") int size
    ) {
        return new ResponseForm<>(
                readingDiaryService.getPopularDiaryFeedsByMemberAndBook(memberId, bookId, page, size));
    }


    @Operation(summary = "독서 일지 작성")
    @PostMapping("/reading-diaries")
    public ResponseForm<DiaryResponse> addDiaryEntry(
            @AuthenticationPrincipal Long memberId,
            @Valid @RequestBody DiaryRequest request) {
        return new ResponseForm<>(readingDiaryService.createDiary(memberId, request));
    }

    @Operation(summary = "독서 일지 수정")
    @PutMapping("/reading-diaries/{diaryId}")
    public ResponseForm<DiaryResponse> updateDiaryEntry(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId,
            @Valid @RequestBody DiaryUpdateRequest request) {
        return new ResponseForm<>(readingDiaryService.updateDiary(memberId, diaryId, request));
    }

    @Operation(summary = "독서 일지 삭제")
    @DeleteMapping("/reading-diaries/{diaryId}")
    public ResponseForm<Void> deleteDiaryEntry(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId) {
        readingDiaryService.deleteDiary(memberId, diaryId);
        return ResponseForm.ok();
    }
} 
