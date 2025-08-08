package server.security.oauth.service;

import server.member.entity.ProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2Factory {

    private final KakaoService kakaoService;
    private final AppleService appleService;
    private final GoogleService googleService;

    public OAuth2Service getProvider(ProviderType providerType) {
        if (providerType.equals(ProviderType.KAKAO)) {
            return kakaoService;
        }
        else if (providerType.equals(ProviderType.APPLE)) {
            return appleService;
        }
        else if (providerType.equals(ProviderType.GOOGLE)) {
            return googleService;
        }
        throw new OAuth2AuthenticationException("PROVIDER_NOT_SUPPORTED: " + providerType.name());
    }
}
