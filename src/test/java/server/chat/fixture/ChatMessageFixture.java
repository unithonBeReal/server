package server.chat.fixture;

import server.chat.domain.ChatMessage;
import server.chat.domain.ChatMessage.ChatMessageBuilder;
import server.chat.domain.MessageType;
import java.util.concurrent.ThreadLocalRandom;

public class ChatMessageFixture {

    /**
     * Domain/Service 테스트용 Fixture
     */
    public static ChatMessage create() {
        return builder().build();
    }

    public static ChatMessageBuilder builder() {
        long randomId = ThreadLocalRandom.current().nextLong(1, 100000);
        return builderWithoutId()
                .id(randomId);
    }

    /**
     * Repository/API 테스트용 Fixture
     */
    public static ChatMessage createWithoutId() {
        return builderWithoutId().build();
    }

    public static ChatMessageBuilder builderWithoutId() {
        long randomNumber = ThreadLocalRandom.current().nextLong(1, 100000);
        return ChatMessage.builder()
                .id(null)
                .content("Test Message " + randomNumber)
                .messageType(MessageType.TEXT)
                .fileUrl(null);

    }
} 
