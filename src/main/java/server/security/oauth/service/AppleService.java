package server.security.oauth.service;


import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.security.oauth.apple.AppleKeyGenerator;
import server.security.oauth.apple.domain.AppleRefreshToken;
import server.security.oauth.dto.AppleUserInfo;
import server.security.oauth.apple.repository.AppleRefreshTokenRepository;
import server.security.oauth.dto.OAuth2UserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;


@Service
@RequiredArgsConstructor
@Slf4j
public class AppleService implements OAuth2Service {

    @Value("${apple.client-id}")
    private String appleClientId;

    private final MemberRepository memberRepository;
    private final AppleRefreshTokenRepository appleRefreshTokenRepository;
    private final WebClient webClient;
    private final AppleKeyGenerator appleKeyGenerator;

    private final JwtPayloadParser jwtPayloadParser;

    @Override
    public OAuth2UserInfo getUser(String idToken) {
        return jwtPayloadParser.parse(idToken, AppleUserInfo.class);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        // Apple 토큰 해지
        AppleRefreshToken appleToken = appleRefreshTokenRepository
                .findByMemberId(member.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPLE_REFRESH_TOKEN_NOT_FOUND));

        revokeAppleToken(appleToken.getRefreshToken());
        appleRefreshTokenRepository.deleteByMemberId(member.getId());
    }

    private void revokeAppleToken(String refreshToken) {
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("client_id", appleClientId);
            params.add("client_secret", appleKeyGenerator.createClientSecret());
            params.add("token", refreshToken);
            params.add("token_type_hint", "refresh_token");

            webClient.post()
                    .uri("https://appleid.apple.com/auth/oauth2/v2/revoke")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(params)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_FAILED_TO_REVOKE_TOKEN);
        }
    }

}
