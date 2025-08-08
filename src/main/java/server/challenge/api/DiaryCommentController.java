package server.challenge.api;

import server.challenge.dto.CommentRequest;
import server.challenge.dto.response.DiaryCommentResponse;
import server.challenge.service.DiaryCommentService;
import server.common.response.CursorPageResponse;
import server.common.response.ResponseForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "diary comment", description = "독서일지 댓글/대댓글 API")
@RestController
@RequestMapping("/api/v2")
@RequiredArgsConstructor
public class DiaryCommentController {

    private final DiaryCommentService diaryCommentService;

    @Operation(summary = "독서일지 댓글 또는 대댓글 생성")
    @PostMapping("/reading-diaries/{diaryId}/comments")
    public ResponseForm<DiaryCommentResponse> createComment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long diaryId,
            @RequestBody @Valid CommentRequest request
    ) {
        return new ResponseForm<>(diaryCommentService.createComment(memberId, diaryId, request));
    }

    @Operation(summary = "댓글 또는 대댓글 삭제")
    @DeleteMapping("/reading-diaries/comments/{commentId}")
    public ResponseForm<Void> deleteComment(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long commentId
    ) {
        diaryCommentService.deleteComment(memberId, commentId);
        return ResponseForm.ok();
    }

    @GetMapping("/reading-diaries/{diaryId}/comments")
    public ResponseForm<CursorPageResponse<DiaryCommentResponse>> findComments(
            @PathVariable Long diaryId,
            @RequestParam(required = false) Long cursorId,
            @RequestParam(defaultValue = "20") int size
    ) {
        return new ResponseForm<>(diaryCommentService.findCommentsByDiary(diaryId, cursorId, size));
    }
} 
