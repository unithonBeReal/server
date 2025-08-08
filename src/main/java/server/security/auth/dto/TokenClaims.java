package server.security.auth.dto;

import server.member.entity.Member;
import server.member.entity.MemberRole;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenClaims {

    private final Long id;
    private final String providerId;
    private final String email;
    private final MemberRole role;

    public static TokenClaims from(Member member) {
        return TokenClaims.builder()
                .id(member.getId())
                .providerId(member.getProviderId())
                .email(member.getEmail())
                .role(member.getRole())
                .build();
    }

}
