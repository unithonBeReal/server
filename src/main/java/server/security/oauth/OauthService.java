package server.security.oauth;

import server.common.CustomException;
import server.common.ErrorCode;
import server.common.error_notification.SenderToDiscord;
import server.member.entity.Member;
import server.member.entity.MemberRole;
import server.member.repository.MemberRepository;
import server.member.service.DeleteMemberService;
import server.member.util.NicknameGenerator;
import server.security.auth.dto.AuthResponse;
import server.security.auth.service.JwtService;
import server.security.oauth.dto.OAuth2UserInfo;
import server.security.oauth.dto.request.LoginRequest;
import server.security.oauth.service.AppleService;
import server.security.oauth.service.GoogleService;
import server.security.oauth.service.JwtPayloadParser;
import server.security.oauth.service.KakaoService;
import server.security.oauth.service.OAuth2Factory;
import server.security.oauth.service.OAuth2Service;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OauthService {

    private final MemberRepository memberRepository;
    private final AppleService appleService;
    private final GoogleService googleService;
    private final KakaoService kakaoService;
    private final OAuth2Factory oAuth2Factory;
    private final DeleteMemberService deleteMemberService;
    private final JwtService jwtService;
    private final SenderToDiscord senderToDiscord;
    private final ObjectMapper objectMapper;
    private final JwtPayloadParser jwtPayloadParser;

    @Value("${profile.default-image}")
    private String defaultProfileImage;

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        switch (member.getProviderType()) {
            case KAKAO -> kakaoService.withdraw(memberId);
            case APPLE -> appleService.withdraw(memberId);
            case GOOGLE -> googleService.withdraw(memberId);
        }

        deleteMemberService.deleteMember(memberId);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        sendOAuthLoginLog(request);

        OAuth2Service oAuth2Service = oAuth2Factory.getProvider(request.providerType());
        OAuth2UserInfo userInfo = oAuth2Service.getUser(request.idToken());

        Member member = findOrSignUp(userInfo);

        return jwtService.generateTokens(member);
    }

    private void sendOAuthLoginLog(LoginRequest request) {
        try {
            Map<String, Object> loginInfo = new HashMap<>();
            loginInfo.put("providerType", request.providerType().toString());
            loginInfo.put("idTokenLength", request.idToken().length());

            String decodedPayload = jwtPayloadParser.decodeJwtPayload(request.idToken());
            if (decodedPayload != null) {
                loginInfo.put("idTokenPayload", decodedPayload);
            } else {
                loginInfo.put("idTokenPayload", "디코딩 실패");
            }

            String jsonContent = objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(loginInfo);

            senderToDiscord.sendLog("🔐 OAuth 로그인 요청",
                    "```json\n" + jsonContent + "\n```");

        } catch (Exception e) {
            log.error("OAuth 로그인 로그 전송 실패", e);
        }
    }

    private Member findOrSignUp(OAuth2UserInfo userInfo) {
        return memberRepository.findByProviderId(userInfo.getProviderId())
                .orElseGet(() -> saveMember(userInfo));
    }

    private Member saveMember(OAuth2UserInfo userInfo) {
        Member member = toEntity(userInfo);
        return memberRepository.save(member);
    }

    private Member toEntity(OAuth2UserInfo userInfo) {
        return Member.builder()
                .providerType(userInfo.getProviderType())
                .providerId(userInfo.getProviderId())
                .email(userInfo.getEmail())
                .nickName(NicknameGenerator.getRandomNickname())
                .role(MemberRole.USER)
                .birthYear(userInfo.getBirthYear())
                .profileImage(userInfo.getProfileImageUrl() == "" ? defaultProfileImage : userInfo.getProfileImageUrl())
                .build();
    }
}
