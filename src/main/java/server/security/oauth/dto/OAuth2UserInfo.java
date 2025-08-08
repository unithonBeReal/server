package server.security.oauth.dto;

import server.member.entity.ProviderType;

public interface OAuth2UserInfo {

    String getProviderId();

    ProviderType getProviderType();

    String getEmail();

    String getBirthYear();
    String getProfileImageUrl();
}
