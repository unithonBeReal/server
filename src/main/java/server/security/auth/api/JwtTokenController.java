package server.security.auth.api;

import io.swagger.v3.oas.annotations.Hidden;
import server.common.response.ResponseForm;
import server.security.auth.dto.AuthResponse;
import server.security.auth.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "jwt", description = "JWT 토큰 발급 관련 API")
@Hidden
public class JwtTokenController {

    private final AuthService authService;

    @PostMapping("/login/accesstoken")
    public ResponseForm<Void> loginByAccessToken(@RequestHeader("Authorization") String accessTokenWithBearer) {
        authService.loginByAccessToken(accessTokenWithBearer);
        return ResponseForm.ok();
    }

    @PostMapping("/renew")
    public ResponseForm<AuthResponse> renewToken(@RequestHeader("Authorization") String refreshTokenWithBearer) {
        return new ResponseForm<>(authService.renewAccessToken(refreshTokenWithBearer));
    }

    @PostMapping("/logout")
    public ResponseForm<Void> logout(@RequestHeader("Authorization") String accessTokenWithBearer) {
        authService.logout(accessTokenWithBearer);
        return ResponseForm.ok();
    }

    @GetMapping("/token-status")
    public ResponseForm<String> getTokenStatus(@AuthenticationPrincipal Long memberId) {
        return new ResponseForm<>(authService.getTokenStatus(memberId));
    }
}
