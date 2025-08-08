package server.notification.repository;

import server.notification.domain.FCMToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FCMTokenRepository extends JpaRepository<FCMToken, Long> {
    Optional<FCMToken> findByUserId(Long userId);
}
