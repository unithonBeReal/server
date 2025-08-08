package server.chat.ably.api;

import server.chat.ably.dto.AblyTokenResponse;
import server.chat.ably.service.AblyService;
import server.common.response.ResponseForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "chat")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/ably")
public class AblyController {

    private final AblyService ablyService;

    @Operation(summary = "클라이언트가 Ably에 연결하기 위한 임시 토큰을 발급받습니다.")
    @PostMapping("/token")
    public ResponseForm<AblyTokenResponse> getAblyToken(@AuthenticationPrincipal Long memberId) {
        return new ResponseForm<>(AblyTokenResponse.from(ablyService.createToken(memberId)));
    }
} 
