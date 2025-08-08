package server.chat.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import server.chat.domain.ChatRoom;
import server.chat.dto.ChatRoomResponse;
import server.chat.fixture.ChatRoomFixture;
import server.chat.repository.ChatMessageRepository;
import server.chat.repository.ChatRoomRepository;
import server.chat.repository.MemberChatRoomRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.fixture.MemberFixture;
import server.member.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private MemberChatRoomRepository memberChatRoomRepository;
    @Mock
    private MemberRepository memberRepository;


    @Nested
    @DisplayName("채팅방 참여")
    class JoinChatRoom {
        private Member member;
        private ChatRoom chatRoom;

        @BeforeEach
        void setUp() {
            member = MemberFixture.create();
            chatRoom = ChatRoomFixture.create();
        }

        @Test
        @DisplayName("성공")
        void success() {
            // given
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(chatRoomRepository.findByIdOrElseThrow(chatRoom.getId())).willReturn(chatRoom);
            given(memberChatRoomRepository.existsByMemberAndChatRoom(member, chatRoom)).willReturn(false);

            // when
            chatRoomService.joinChatRoom(member.getId(), chatRoom.getId());

            // then
            verify(memberChatRoomRepository).save(any());
            assertThat(chatRoom.getParticipantCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("실패 - 이미 참여한 유저")
        void fail_alreadyJoined() {
            // given
            given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
            given(chatRoomRepository.findByIdOrElseThrow(chatRoom.getId())).willReturn(chatRoom);
            given(memberChatRoomRepository.existsByMemberAndChatRoom(member, chatRoom)).willReturn(true);

            Long memberId = member.getId();
            Long chatRoomId = chatRoom.getId();

            // when & then
            assertThatThrownBy(() -> chatRoomService.joinChatRoom(memberId, chatRoomId))
                    .isInstanceOf(CustomException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.ALREADY_IN_CHAT_ROOM);

            verify(memberChatRoomRepository, never()).save(any());
        }
    }

    @Test
    @DisplayName("나의 채팅방 목록 조회")
    void getMyChatRooms() {
        // given
        Member member = MemberFixture.create();

        ChatRoom room1 = ChatRoomFixture.create();
        ChatRoom room2 = ChatRoomFixture.create();
        List<ChatRoom> chatRooms = List.of(room1, room2);

        given(memberRepository.findByIdOrElseThrow(member.getId())).willReturn(member);
        given(memberChatRoomRepository.findChatRoomsByMember(member)).willReturn(chatRooms);

        // when
        List<ChatRoomResponse> responses = chatRoomService.getMyChatRooms(member.getId());

        // then
        assertThat(responses).hasSize(2);
    }
} 
