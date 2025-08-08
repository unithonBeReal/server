package server.challenge.fixture;

import server.challenge.domain.ReadingDiary;
import server.challenge.domain.ReadingDiary.ReadingDiaryBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class ReadingDiaryFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static ReadingDiary create() {
        return builder().build();
    }

    public static ReadingDiaryBuilder builder() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId().id(randomNumber);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static ReadingDiary createWithoutId() {
        return builderWithoutId().build();
    }

    public static ReadingDiaryBuilder builderWithoutId() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return ReadingDiary.builder()
                .id(null)
                .content("테스트 일기 내용 " + randomNumber);
    }
} 
