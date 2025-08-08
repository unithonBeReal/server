package server.security.auth.domain;

import server.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;



@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
public class RefreshToken extends BaseTimeEntity {
    @Id
    private Long memberId;

    @Column(name = "refresh_token", length = 4096)
    private String refreshToken;

    @Builder
    public RefreshToken(Long memberId, String refreshToken) {
        this.memberId = memberId;
        this.refreshToken = refreshToken;
        //this.refreshExpiration = refreshExpiration;
    }

    public RefreshToken updateRefreshToken(String newRefreshToken){
        this.refreshToken = newRefreshToken;
        return this;
    }
}
