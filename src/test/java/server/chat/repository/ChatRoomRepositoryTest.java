package server.chat.repository;

import server.chat.domain.ChatRoom;
import server.chat.fixture.ChatRoomFixture;
import server.config.TestQuerydslConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(TestQuerydslConfig.class)
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Test
    @DisplayName("채팅방을 저장하고 ID로 조회한다")
    void saveAndFindById() {
        // given
        // `createWithoutId()`로 ID가 없는 fixture 생성
        ChatRoom newChatRoom = ChatRoomFixture.createWithoutId();
        
        // when
        ChatRoom savedChatRoom = chatRoomRepository.save(newChatRoom);
        ChatRoom foundChatRoom = chatRoomRepository.findById(savedChatRoom.getId()).orElse(null);

        // then
        assertThat(savedChatRoom.getId()).isNotNull();
        assertThat(foundChatRoom).isNotNull();
        assertThat(foundChatRoom.getId()).isEqualTo(savedChatRoom.getId());
        assertThat(foundChatRoom.getName()).isEqualTo(newChatRoom.getName());
    }
} 
