package server.security.oauth.apple.repository;

import server.security.oauth.apple.domain.AppleRefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppleRefreshTokenRepository extends JpaRepository<AppleRefreshToken, Long> {
    Optional<AppleRefreshToken> findByMemberId(Long memberId);

    void deleteByMemberId(Long id);
}
