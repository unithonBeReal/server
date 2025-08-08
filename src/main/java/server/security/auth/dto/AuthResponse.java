package server.security.auth.dto;


import server.member.entity.ProviderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private Long memberId;
    private String nickName;
    private String profileImage;
    private ProviderType providerType;
    private String email;
    private String accessToken;
    private String refreshToken;
    private Date accessTokenExpiration;
    private Date refreshTokenExpiration;
}
