package server.security.oauth.dto;

import server.member.entity.ProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppleUserInfo implements OAuth2UserInfo {
    private String iss;         // https://appleid.apple.com
    private String sub;         // 사용자 고유 식별자
    private String aud;         // client_id (bundle id)
    private Long iat;          // 토큰 발급 시간 (Unix epoch seconds)
    private Long exp;          // 토큰 만료 시간 (Unix epoch seconds)
    private String nonce;      // (선택) 클라이언트 세션 연결용
    private Boolean nonce_supported;  // nonce 지원 플랫폼 여부
    private String email;       // 실제 이메일 또는 프록시 이메일 (없을 수 있음)
    private Boolean email_verified;   // 이메일 검증 여부

    @Override
    public String getProviderId() {
        return this.sub;
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.APPLE;
    }

    @Override
    public String getBirthYear() {
        return "";
    }

    @Override
    public String getEmail() {
        return this.email == null ? "" : this.email;
    }

    @Override
    public String getProfileImageUrl() {
        return "";
    }
}
