package server.challenge.service;

import server.challenge.domain.ReadingDiaryStatistic;
import server.challenge.repository.ReadingDiaryStatisticsRepository;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PopularityScoreScheduler {

    private final StringRedisTemplate redisTemplate;
    private final ReadingDiaryStatisticsRepository statisticsRepository;
    private final ReadingDiaryStatisticService readingDiaryStatisticService; // Redis 조회용 서비스

    private static final String DIRTY_DIARIES_KEY = "diaries:dirty";
    private static final int BATCH_SIZE = 1000; // 한 번에 처리할 작업량

    @Scheduled(cron = "0 0 4 * * *", zone = "Asia/Seoul")
    @Transactional
    public void syncStatisticsInBatches() {
        log.info("DB-Redis 통계 동기화 스케줄을 시작합니다. (배치 처리 방식)");

        long totalProcessedCount = 0;

        // Set에 처리할 ID가 남아있는 동안 계속 반복
        while (Boolean.TRUE.equals(redisTemplate.hasKey(DIRTY_DIARIES_KEY))
                && redisTemplate.opsForSet().size(DIRTY_DIARIES_KEY) > 0) {
            // 1. SPOP 명령어로 Set에서 BATCH_SIZE만큼의 ID를 '꺼내오면서' 안전하게 삭제합니다.
            List<String> diaryIdStrings = redisTemplate.opsForSet().pop(DIRTY_DIARIES_KEY, BATCH_SIZE);

            if (diaryIdStrings == null || diaryIdStrings.isEmpty()) {
                break; // 처리할 ID가 더 이상 없으면 루프 종료
            }

            // 2. 단일 배치 처리 로직을 실행합니다.
            processSingleBatch(diaryIdStrings.stream().map(Long::valueOf).toList());

            totalProcessedCount += diaryIdStrings.size();
            log.info("통계 배치 처리 중... (현재까지 {}개 처리)", totalProcessedCount);
        }

        if (totalProcessedCount > 0) {
            log.info("총 {}개의 독서일지에 대한 통계 동기화를 완료했습니다.", totalProcessedCount);
        } else {
            log.info("통계 동기화 스케줄을 완료했습니다. (업데이트 사항 없음)");
        }
    }

    /**
     * 단일 배치에 포함된 독서일지들의 통계를 DB에 업데이트합니다.
     *
     * @param batchIds 처리할 독서일지 ID 목록
     */
    private void processSingleBatch(List<Long> batchIds) {
        // Redis에서 이 ID들에 해당하는 최신 카운트 정보를 일괄 조회합니다.
        Map<Long, Map<CountType, Long>> latestCountsFromRedis = readingDiaryStatisticService.getCounts(batchIds);

        // DB에서 이 ID들에 해당하는 통계 엔티티들을 일괄 조회합니다.
        List<ReadingDiaryStatistic> statisticsInDB = statisticsRepository.findAllByReadingDiaryIdIn(batchIds);

        // 각 엔티티를 Redis의 최신 데이터로 업데이트합니다.
        for (ReadingDiaryStatistic statistic : statisticsInDB) {
            Long diaryId = statistic.getReadingDiary().getId();
            Map<CountType, Long> counts = latestCountsFromRedis.get(diaryId);

            if (counts != null) {
                // DB 엔티티의 상태를 갱신합니다.
                statistic.apply(
                        counts.getOrDefault(CountType.VIEW, 0L).intValue(),
                        counts.getOrDefault(CountType.LIKE, 0L).intValue(),
                        counts.getOrDefault(CountType.COMMENT, 0L).intValue()
                );
            }
        }
    }
}
