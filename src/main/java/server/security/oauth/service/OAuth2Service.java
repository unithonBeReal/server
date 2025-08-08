package server.security.oauth.service;

import server.security.oauth.dto.OAuth2UserInfo;

public interface OAuth2Service {

    OAuth2UserInfo getUser(String idToken);
}
