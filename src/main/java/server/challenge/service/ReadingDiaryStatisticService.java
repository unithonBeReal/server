package server.challenge.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReadingDiaryStatisticService {

    private static final String DIARY_KEY_PREFIX = "diaries:";
    private static final String DIRTY_DIARIES_KEY = "diaries:dirty";
    private final StringRedisTemplate redisTemplate;

    public void increaseViewCount(Long diaryId, Long memberId) {
        String userViewLogKey = DIARY_KEY_PREFIX + diaryId + ":views:log";
        Long addResult = redisTemplate.opsForSet().add(userViewLogKey, String.valueOf(memberId));
        redisTemplate.expire(userViewLogKey, 1, java.util.concurrent.TimeUnit.DAYS);

        boolean isFirstViewIn24Hours = addResult != null && addResult > 0;
        if (isFirstViewIn24Hours) {
            incrementCount(diaryId, CountType.VIEW);
        }
    }

    public void incrementCount(Long diaryId, CountType type) {
        String counterKey = DIARY_KEY_PREFIX + diaryId + ":" + type.name().toLowerCase();
        redisTemplate.opsForValue().increment(counterKey);
        redisTemplate.opsForSet().add(DIRTY_DIARIES_KEY, String.valueOf(diaryId));
    }

    public void decrementCount(Long diaryId, CountType type, long amount) {
        String counterKey = DIARY_KEY_PREFIX + diaryId + ":" + type.name().toLowerCase();
        redisTemplate.opsForValue().decrement(counterKey, amount);
        redisTemplate.opsForSet().add(DIRTY_DIARIES_KEY, String.valueOf(diaryId));
    }

    public Map<Long, Map<CountType, Long>> getCounts(List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<String> keys = new ArrayList<>();
        for (Long diaryId : diaryIds) {
            for (CountType type : CountType.values()) {
                keys.add(DIARY_KEY_PREFIX + diaryId + ":" + type.name().toLowerCase());
            }
        }

        List<String> values = redisTemplate.opsForValue().multiGet(keys);

        Map<Long, Map<CountType, Long>> result = new HashMap<>();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = (values != null && values.get(i) != null) ? values.get(i) : "0";
            long count = Long.parseLong(value);

            // 예: "diaries:101:view" -> parts[0]="diaries", parts[1]="101", parts[2]="view"
            String[] parts = key.split(":");
            if (parts.length >= 3) {
                Long diaryId = Long.parseLong(parts[1]);
                CountType type = CountType.valueOf(parts[2].toUpperCase());
                result.computeIfAbsent(diaryId, k -> new EnumMap<>(CountType.class)).put(type, count);
            }
        }
        return result;
    }

    public void deleteCounts(List<Long> diaryIds) {
        if (diaryIds == null || diaryIds.isEmpty()) {
            return;
        }

        List<String> keysToDelete = new ArrayList<>();
        for (Long diaryId : diaryIds) {
            for (CountType type : CountType.values()) {
                keysToDelete.add(DIARY_KEY_PREFIX + diaryId + ":" + type.name().toLowerCase());
            }
            // 24시간 조회 기록 로그 Set도 함께 삭제
            keysToDelete.add(DIARY_KEY_PREFIX + diaryId + ":views:log");
        }

        redisTemplate.delete(keysToDelete);

        String[] dirtyIdsToRemove = diaryIds.stream()
                .map(String::valueOf)
                .toArray(String[]::new);

        redisTemplate.opsForSet().remove(DIRTY_DIARIES_KEY, (Object[]) dirtyIdsToRemove);
    }
} 
