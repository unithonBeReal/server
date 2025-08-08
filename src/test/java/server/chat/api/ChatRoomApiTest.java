package server.chat.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import server.chat.domain.ChatCategory;
import server.chat.domain.ChatRoom;
import server.chat.repository.ChatRoomRepository;
import server.chat.repository.MemberChatRoomRepository;
import server.chat.service.ChatRoomService;
import server.config.SecurityTestUtils;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ChatRoomApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private MemberChatRoomRepository memberChatRoomRepository;

    @Autowired
    private ChatRoomService chatRoomService;

    @Autowired
    private SecurityTestUtils securityTestUtils;

    private Member member;
    private RequestPostProcessor mockUser;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(MemberFixture.createWithoutId());
        mockUser = securityTestUtils.mockUser(member);
    }

    @Test
    @DisplayName("사용자는 채팅방에서 나갈 수 있다.")
    void leaveChatRoom() throws Exception {
        // given
        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder().name("Test Room").category(ChatCategory.BESTSELLER).build());
        chatRoomService.joinChatRoom(member.getId(), chatRoom.getId());
        
        // when
        mockMvc.perform(delete("/api/v2/chat/rooms/" + chatRoom.getId())
                        .with(mockUser)
                        .with(csrf()))
                .andExpect(status().isOk());
        
        // then
        ChatRoom foundChatRoom = chatRoomRepository.findByIdOrElseThrow(chatRoom.getId());
        
        assertThat(memberChatRoomRepository.count()).isZero();
        assertThat(foundChatRoom.getParticipantCount()).isZero();
    }
} 
