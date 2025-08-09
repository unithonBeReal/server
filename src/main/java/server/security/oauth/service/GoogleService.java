package server.security.oauth.service;

import server.common.CustomException;
import server.common.ErrorCode;
import server.security.oauth.dto.GoogleUserInfo;
import server.security.oauth.dto.OAuth2UserInfo;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleService implements OAuth2Service {

    private List<String> googleClientIds;

    @Override
    public OAuth2UserInfo getUser(String idTokenString) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),
                    new GsonFactory())
                    // setAudience에 client ID 목록 전체를 전달합니다.
                    .setAudience(googleClientIds)
                    .build();

            GoogleIdToken idToken = verifier.verify(idTokenString);
            if (idToken == null) {
                throw new CustomException(ErrorCode.GOOGLE_IDTOKEN_VALIDATION_FAIL);
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            return GoogleUserInfo.from(payload);
        } catch (GeneralSecurityException | IOException e) {
            throw new CustomException(ErrorCode.GOOGLE_IDTOKEN_VALIDATION_FAIL);
        }
    }

    public void withdraw(Long memberId) {
        log.warn("Google 회원은 서버에서 직접 연동해제가 불가능합니다. 클라이언트에서 Google SDK를 사용하여 해제해야 합니다. memberId: {}", memberId);
        // 현재 서버에서는 구글로부터 받은 access/refresh 토큰을 저장하고 있지 않으므로,
        // 서버에서 직접 구글 계정의 연동을 해제하는 API를 호출할 수 없습니다.
        // 클라이언트에서 구글 로그아웃 및 연동 해제 처리가 필요합니다.
    }
} 
