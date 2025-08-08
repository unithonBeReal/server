package server.challenge.dto.response;

import server.challenge.domain.DiaryComment;
import server.member.dto.MinimumMemberProfile;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;

@Builder
public record DiaryCommentResponse(
        Long commentId,
        String content,
        MinimumMemberProfile writer,
        LocalDateTime createdAt,
        List<DiaryReplyResponse> replies
) {

    public static DiaryCommentResponse of(DiaryComment comment, List<DiaryReplyResponse> replies) {
        return DiaryCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .writer(new MinimumMemberProfile(comment.getMember().getId(), comment.getMember().getNickName(),
                        comment.getMember().getProfileImage()))
                .createdAt(comment.getCreatedDate())
                .replies(replies)
                .build();
    }

    public static DiaryCommentResponse from(DiaryComment comment) {
        List<DiaryReplyResponse> replyResponses = comment.getChildren().stream()
                .map(DiaryReplyResponse::from)
                .toList();

        return DiaryCommentResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .writer(new MinimumMemberProfile(comment.getMember().getId(), comment.getMember().getNickName(),
                        comment.getMember().getProfileImage()))
                .createdAt(comment.getCreatedDate())
                .replies(replyResponses) // 변환된 대댓글 목록을 설정합니다.
                .build();
    }
} 
