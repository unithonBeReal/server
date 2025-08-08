package server.chat.fixture;

import server.chat.domain.MemberChatRoom;
import server.chat.domain.MemberChatRoom.MemberChatRoomBuilder;
import java.util.concurrent.ThreadLocalRandom;

public class MemberChatRoomFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static MemberChatRoom create() {
        return builder().build();
    }

    public static MemberChatRoomBuilder builder() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId().id(randomId);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static MemberChatRoom createWithoutId() {
        return builderWithoutId().build();
    }

    public static MemberChatRoomBuilder builderWithoutId() {
        return MemberChatRoom.builder()
                .id(null);
    }
} 
