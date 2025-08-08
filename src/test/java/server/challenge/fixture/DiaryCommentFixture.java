package server.challenge.fixture;

import server.challenge.domain.DiaryComment;
import server.challenge.domain.DiaryComment.DiaryCommentBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class DiaryCommentFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static DiaryComment create() {
        return builder().build();
    }

    public static DiaryCommentBuilder builder() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return DiaryComment
                .builder()
                .id(randomNumber);
    }
}
