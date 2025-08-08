package server.chat.fixture;

import server.chat.domain.ChatCategory;
import server.chat.domain.ChatRoom;
import server.chat.domain.ChatRoom.ChatRoomBuilder;

import java.util.concurrent.ThreadLocalRandom;

public class ChatRoomFixture {


    /**
     * Domain/Service 테스트용 Fixture. 랜덤 ID를 가진 ChatRoom 객체를 생성합니다.
     */
    public static ChatRoom create() {
        return builder().build();
    }

    public static ChatRoomBuilder builder() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId()
                .id(randomId);
    }

    /**
     * Repository/API 테스트용 Fixture. ID가 없는 ChatRoom 객체를 생성합니다.
     */
    public static ChatRoom createWithoutId() {
        return builderWithoutId().build();
    }

    public static ChatRoomBuilder builderWithoutId() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return ChatRoom.builder()
                .id(null)
                .name("Test ChatRoom " + randomNumber)
                .category(ChatCategory.BESTSELLER)
                .participantCount(0);
    }
} 
