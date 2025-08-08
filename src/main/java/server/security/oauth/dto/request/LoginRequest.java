package server.security.oauth.dto.request;

import server.member.entity.ProviderType;

public record LoginRequest(ProviderType providerType, String idToken) {
}
