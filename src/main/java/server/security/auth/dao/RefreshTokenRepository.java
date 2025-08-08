package server.security.auth.dao;

import server.security.auth.domain.RefreshToken;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;


public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMemberId(Long memberId);
    Optional<RefreshToken> findByRefreshToken(String refreshToken);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.memberId = :memberId")
    Optional<RefreshToken> findByMemberIdWithLock(@Param("memberId") Long memberId);

    void deleteByMemberId(Long memberId);

    void deleteByRefreshToken(String refreshToken);

    boolean existsByMemberId(Long memberId);
}
