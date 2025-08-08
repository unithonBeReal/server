package server.challenge.dto.response;

import server.challenge.domain.DiaryComment;
import server.member.dto.MinimumMemberProfile;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record DiaryReplyResponse(
        Long commentId,
        String content,
        MinimumMemberProfile writer,
        LocalDateTime createdAt
) {

    public static DiaryReplyResponse from(DiaryComment comment) {
        return DiaryReplyResponse.builder()
                .commentId(comment.getId())
                .content(comment.getContent())
                .writer(new MinimumMemberProfile(comment.getMember().getId(), comment.getMember().getNickName(),
                        comment.getMember().getProfileImage()))
                .createdAt(comment.getCreatedDate())
                .build();
    }
} 
