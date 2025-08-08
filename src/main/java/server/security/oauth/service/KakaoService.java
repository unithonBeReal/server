package server.security.oauth.service;

import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.security.oauth.dto.KakaoUserInfo2;
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
public class KakaoService implements OAuth2Service {

    private final JwtPayloadParser jwtParser;
    private final MemberRepository memberRepository;
    private final WebClient webClient;

    @Value("${kakao.admin-key}")
    private String kakaoAdminKey;

    private static final String UNLINK_URI = "https://kapi.kakao.com/v1/user/unlink";

    @Override
    public OAuth2UserInfo getUser(String idToken) {
        return jwtParser.parse(idToken, KakaoUserInfo2.class);
    }

    @Transactional
    public void withdraw(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("target_id_type", "user_id");
        formData.add("target_id", member.getProviderId());

        try {
            webClient.post()
                    .uri(UNLINK_URI)
                    .header("Authorization", "KakaoAK " + kakaoAdminKey)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .bodyValue(formData)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            log.info("카카오 계정 연결 해제 완료. memberId: {}", memberId);
        } catch (Exception e) {
            log.error("카카오 계정 연결 해제 실패. memberId: {}", memberId, e);
        }
    }
} 
