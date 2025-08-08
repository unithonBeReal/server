package server.security.oauth;

import server.common.response.ResponseForm;
import server.security.auth.dto.AuthResponse;
import server.security.oauth.dto.request.LoginRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "oauth", description = "소셜 로그인 및 인증 관련 API")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class OauthController {

    private final OauthService oauthService;

    @DeleteMapping("/withdraw")
    public ResponseForm<Void> withdraw(@AuthenticationPrincipal Long memberId) {
        oauthService.withdraw(memberId);
        return ResponseForm.ok();
    }

    @PostMapping("/login")
    public ResponseForm<AuthResponse> login(
            @RequestBody @Valid LoginRequest request
    ) {
        return new ResponseForm<>(oauthService.login(request));
    }

}
