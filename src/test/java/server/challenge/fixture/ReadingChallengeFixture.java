package server.challenge.fixture;

import server.challenge.domain.ReadingChallenge;
import server.challenge.domain.ReadingChallenge.ReadingChallengeBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class ReadingChallengeFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static ReadingChallenge create() {
        return builder().build();
    }

    public static ReadingChallengeBuilder builder() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId().id(randomId);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static ReadingChallenge createWithoutId() {
        return builderWithoutId().build();
    }

    public static ReadingChallengeBuilder builderWithoutId() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100);
        int randomNumberInt = Long.valueOf(randomNumber).intValue();
        return ReadingChallenge.builder()
                .id(null)
                .currentPage(0)
                .totalPages(99999999)
                .completed(false)
                .completedAt(null)
                .abandoned(false)
                .abandonedAt(null)
                .rating(randomNumberInt % 5)
                .recommendationScore(randomNumberInt % 5);
    }
}
