package server.challenge.fixture;

import server.challenge.domain.ReadingDiaryStatistic;
import server.challenge.domain.ReadingDiaryStatistic.ReadingDiaryStatisticBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class ReadingDiaryStatisticFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static ReadingDiaryStatistic create() {
        return builder().build();
    }

    public static ReadingDiaryStatisticBuilder builder() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        int viewCount = ThreadLocalRandom.current().nextInt(0, 1000);
        int likeCount = ThreadLocalRandom.current().nextInt(0, 1000);
        int commentCount = ThreadLocalRandom.current().nextInt(0, 1000);

        // 테스트 요구사항: 인기점수 = 조회수 + 좋아요수 + 댓글수
        double popularityScore = viewCount + likeCount + commentCount;

        return ReadingDiaryStatistic.builder()
                .id(randomNumber)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .popularityScore(popularityScore);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static ReadingDiaryStatistic createWithoutId() {
        return builderWithoutId().build();
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static ReadingDiaryStatisticBuilder builderWithoutId() {
        int viewCount = ThreadLocalRandom.current().nextInt(0, 1000);
        int likeCount = ThreadLocalRandom.current().nextInt(0, 1000);
        int commentCount = ThreadLocalRandom.current().nextInt(0, 1000);

        double popularityScore = viewCount + likeCount + commentCount;

        return ReadingDiaryStatistic.builder()
                .id(null)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .popularityScore(popularityScore);
    }
} 
