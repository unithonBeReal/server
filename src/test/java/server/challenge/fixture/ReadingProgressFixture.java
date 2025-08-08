package server.challenge.fixture;

import server.challenge.domain.ReadingProgress;
import server.challenge.domain.ReadingProgress.ReadingProgressBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class ReadingProgressFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static ReadingProgress create() {
        return builder().build();
    }

    public static ReadingProgressBuilder builder() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId().id(randomNumber);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static ReadingProgress createWithoutId() {
        return builderWithoutId().build();
    }

    public static ReadingProgressBuilder builderWithoutId() {
        int randomNumber = ThreadLocalRandom.current().nextInt(1, 10000);
        return ReadingProgress.builder()
                .id(null)
                .startPage(randomNumber)
                .endPage(randomNumber + 1);
    }
}
