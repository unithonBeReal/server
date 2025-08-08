package server.security.oauth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtPayloadParser {

    private final ObjectMapper mapper = new ObjectMapper();

    public <T> T parse(String idToken, Class<T> targetClass) {
        try {
            Claims claims = parseClaimsUnsafe(idToken);
            return mapper.convertValue(claims, targetClass);
        } catch (Exception e) {
            throw new IllegalArgumentException("JWT 파싱 실패: " + e.getMessage(), e);
        }
    }

    public String decodeJwtPayload(String idToken) {
        try {
            Claims claims = parseClaimsUnsafe(idToken);
            return mapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(claims);
        } catch (Exception e) {
            log.debug("JWT 페이로드 디코딩 실패", e);
            return null;
        }
    }

    /**
     * 서명 검증 없이 JWT Claims를 추출 (OAuth idToken은 서명된 토큰이지만 내용만 확인)
     */
    private Claims parseClaimsUnsafe(String jwt) {
        try {
            // JWT를 3부분으로 분리 (header.payload.signature)
            String[] parts = jwt.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("올바르지 않은 JWT 형식");
            }

            // 서명 부분을 제거하고 빈 서명으로 대체하여 파싱
            String unsignedJwt = parts[0] + "." + parts[1] + ".";
            
            return Jwts.parserBuilder()
                    .build()
                    .parseClaimsJwt(unsignedJwt)
                    .getBody();
                    
        } catch (Exception e) {
            throw new RuntimeException("JWT 클레임 추출 실패", e);
        }
    }
}
