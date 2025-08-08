package server.chat.ably.dto;

import io.ably.lib.rest.Auth;

public record AblyTokenResponse(String token) {
    public static AblyTokenResponse from(Auth.TokenDetails tokenDetails) {
        return new AblyTokenResponse(tokenDetails.token);
    }
} 
