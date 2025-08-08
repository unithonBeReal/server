package server.security.auth.service;

import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import server.security.auth.dao.RefreshTokenRepository;
import server.security.auth.dto.AuthResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import server.security.auth.domain.RefreshToken;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    @Transactional
    public AuthResponse renewAccessToken(String refreshTokenWithBearer) {
        String refreshToken = jwtService.resolveAndValidateToken(refreshTokenWithBearer);

        jwtService.validateToken(refreshToken);

        Long memberId = jwtService.getClaims(refreshToken).get("id", Long.class);
        
        // 토큰 만료일 확인
        Date expiration = jwtService.getClaims(refreshToken).getExpiration();
        Date now = new Date();
        long remainingTimeMs = expiration.getTime() - now.getTime();
        long remainingDays = remainingTimeMs / (1000 * 60 * 60 * 24);
        long remainingHours = (remainingTimeMs % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        
        log.info("토큰 갱신 요청 - memberId: {}, 토큰 만료일: {}, 남은 기간: {}일 {}시간", 
                memberId, expiration, remainingDays, remainingHours);

        try {
            // 비관적 락을 사용하여 동시성 문제 해결
            Optional<RefreshToken> storedTokenOpt = refreshTokenRepository.findByMemberIdWithLock(memberId);
            
            if (storedTokenOpt.isEmpty()) {
                throw new CustomException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
            }
            
            RefreshToken storedToken = storedTokenOpt.get();
            String storedRefreshToken = storedToken.getRefreshToken();
            
            log.info("토큰 비교 - memberId: {}, DB토큰: {}, 요청토큰: {}",
                    memberId, 
                    storedRefreshToken,
                    refreshToken);
            
            if (!storedRefreshToken.equals(refreshToken)) {
                log.warn("리프레시 토큰이 일치하지 않습니다. memberId: {}, DB토큰길이: {}, 요청토큰길이: {}", 
                        memberId, storedRefreshToken.length(), refreshToken.length());
                throw new CustomException(ErrorCode.REFRESH_TOKEN_MISMATCH);
            }

            Member member = memberRepository.findById(memberId)
                    .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

            log.info("토큰 갱신 성공 - memberId: {}", memberId);
            return jwtService.generateTokens(member);
            
        } catch (PessimisticLockingFailureException e) {
            throw new CustomException(ErrorCode.CONCURRENCY_ERROR, "리프레시 토큰 갱신 중 락 충돌 발생. memberId: " + memberId + " " + e);
        }
    }

    @Transactional
    public void loginByAccessToken(String accessTokenWithBearer) {
        String accessToken = jwtService.resolveAndValidateToken(accessTokenWithBearer);
        jwtService.validateToken(accessToken);
    }

    @Transactional
    public void logout(String accessTokenWithBearer) {
        String accessToken = jwtService.resolveAndValidateToken(accessTokenWithBearer);
        
        Long memberId = jwtService.getClaims(accessToken).get("id", Long.class);

        refreshTokenRepository.findByMemberId(memberId)
                .ifPresent(refreshTokenRepository::delete);
        
        log.info("사용자 로그아웃 완료. memberId: {}", memberId);
    }

    @Transactional(readOnly = true)
    public String getTokenStatus(Long memberId) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByMemberId(memberId);
        
        if (tokenOpt.isEmpty()) {
            return "토큰 없음";
        }
        
        RefreshToken token = tokenOpt.get();
        String refreshToken = token.getRefreshToken();
        
        return String.format("토큰 존재 - 길이: %d, 끝 4자리: %s", 
                refreshToken.length(), 
                refreshToken.substring(Math.max(0, refreshToken.length() - 4)));
    }
}
