package server.security.oauth.dto;

import server.member.entity.ProviderType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoUserInfo2 implements OAuth2UserInfo {
    private String sub;
    private String email;
    private String picture;
    private String birthyear;

    @Override
    public String getProviderId() {
        return sub;
    }

    @Override
    public String getEmail() {
        return this.email == null ? "" : this.email;
    }

    @Override
    public String getProfileImageUrl() {
        return picture == null ? "" : picture;
    }

    @Override
    public String getBirthYear() {
        return birthyear == null ? "" : birthyear;
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.KAKAO;
    }
}
