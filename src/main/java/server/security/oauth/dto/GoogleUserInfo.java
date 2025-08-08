package server.security.oauth.dto;

import server.member.entity.ProviderType;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import lombok.Builder;

@Builder
public class GoogleUserInfo implements OAuth2UserInfo {

    private String providerId;
    private String email;
    private String profileImageUrl;
    private ProviderType providerType;

    public static GoogleUserInfo from(GoogleIdToken.Payload payload) {
        return GoogleUserInfo.builder()
                .providerId(payload.getSubject())
                .email(payload.getEmail())
                .profileImageUrl((String) payload.get("picture"))
                .providerType(ProviderType.GOOGLE)
                .build();
    }

    @Override
    public String getProviderId() {
        return providerId;
    }

    @Override
    public ProviderType getProviderType() {
        return ProviderType.GOOGLE;
    }

    @Override
    public String getEmail() {
        return this.email == null ? "" : this.email;
    }

    @Override
    public String getBirthYear() {
        return null;
    }

    @Override
    public String getProfileImageUrl() {
        return profileImageUrl == null ? "" : this.profileImageUrl;
    }
} 
