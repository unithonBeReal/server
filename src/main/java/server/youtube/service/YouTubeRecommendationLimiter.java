package server.youtube.service;

import server.common.CustomException;
import server.common.ErrorCode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class YouTubeRecommendationLimiter {

    private final StringRedisTemplate redisTemplate;
    private static final int DAILY_LIMIT = 20;
    private static final String KEY_PREFIX = "rate_limit:recommend:youtube:";

    /**
     * 사용자의 일일 API 호출 횟수를 확인하고 제한을 초과하면 예외를 발생시킵니다.
     * @param memberId 사용자 ID
     */
    public void checkLimit(Long memberId) {
        String dailyKey = buildDailyKey(memberId);
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        Long currentCount = ops.increment(dailyKey);

        if (currentCount == null) {
            // Redis가 null을 반환하는 비정상적인 경우에 대한 방어 코드
            throw new IllegalStateException("Redis의 increment 연산에 실패했습니다 (API 호출 제한)");
        }

        // 해당 키가 처음 생성되었을 때 (즉, 오늘의 첫 요청일 때) 만료 시간을 하루로 설정합니다.
        if (currentCount == 1L) {
            redisTemplate.expire(dailyKey, Duration.ofDays(1));
        }

        if (currentCount > DAILY_LIMIT) {
            throw new CustomException(ErrorCode.RECOMMENDATION_RATE_LIMIT_EXCEEDED);
        }
    }

    /**
     * 사용자 ID와 현재 날짜를 조합하여 Redis 키를 생성합니다. (예: rate_limit:recommend:youtube:123:2023-10-27)
     */
    private String buildDailyKey(Long memberId) {
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE); // yyyy-MM-dd
        return KEY_PREFIX + memberId + ":" + today;
    }
} 
