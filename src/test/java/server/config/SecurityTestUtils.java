package server.config;

import server.member.entity.Member;
import server.security.auth.dto.TokenClaims;
import server.security.auth.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

@Component
@RequiredArgsConstructor
public class SecurityTestUtils {

    private final JwtService jwtService;

    /**
     * 테스트용 사용자를 받아, 실제 JWT를 생성하고 Authorization 헤더에 담아주는 RequestPostProcessor를 반환합니다.
     *
     * @param member 인증 처리를 할 Member 객체
     * @return Authorization 헤더가 추가된 RequestPostProcessor
     */
    public RequestPostProcessor mockUser(Member member) {
        return request -> {
            // 1. 실제 로직과 유사하게 TokenClaims DTO 생성
            TokenClaims tokenClaims = TokenClaims.builder()
                    .id(member.getId())
                    .email(member.getEmail())
                    .providerId(member.getProviderId())
                    .role(member.getRole())
                    .build();

            // 2. generateAccessToken 메소드를 사용하여 토큰 생성
            String token = jwtService.generateAccessToken(tokenClaims);

            // 3. 요청 헤더에 Authorization 토큰 추가
            request.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
            return request;
        };
    }
}
