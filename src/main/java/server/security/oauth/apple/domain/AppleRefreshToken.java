package server.security.oauth.apple.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class AppleRefreshToken {
    @Id
    private Long memberId;
    @Column(length = 4096) // 애플 리프레쉬 토큰이 255자 넘어서 필수
    private String refreshToken;

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
