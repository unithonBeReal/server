package server.chat.dto.response;

import server.member.entity.Member;
import lombok.Builder;

@Builder
public record ParticipantInfo(
        Long memberId,
        String nickname,
        String profileImageUrl
) {
    public static ParticipantInfo from(Member member) {
        return ParticipantInfo.builder()
                .memberId(member.getId())
                .nickname(member.getNickName())
                .profileImageUrl(member.getProfileImage())
                .build();
    }
} 
