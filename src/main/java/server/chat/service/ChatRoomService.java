package server.chat.service;

import server.chat.domain.ChatRoom;
import server.chat.domain.MemberChatRoom;
import server.chat.dto.ChatRoomResponse;
import server.chat.dto.response.ChatRoomParticipantsResponse;
import server.chat.dto.response.ParticipantInfo;
import server.chat.repository.ChatRoomRepository;
import server.chat.repository.MemberChatRoomRepository;
import server.common.CustomException;
import server.common.ErrorCode;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberChatRoomRepository memberChatRoomRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void joinChatRoom(Long memberId, Long roomId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        ChatRoom chatRoom = chatRoomRepository.findByIdOrElseThrow(roomId);

        if (memberChatRoomRepository.existsByMemberAndChatRoom(member, chatRoom)) {
            throw new CustomException(ErrorCode.ALREADY_IN_CHAT_ROOM);
        }

        MemberChatRoom memberChatRoom = MemberChatRoom.builder()
                .member(member)
                .chatRoom(chatRoom)
                .build();
        memberChatRoomRepository.save(memberChatRoom);
        chatRoom.incrementParticipantCount();
    }

    @Transactional
    public void leaveChatRoom(Long memberId, Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findByIdOrElseThrow(roomId);
        memberChatRoomRepository.deleteByMemberIdAndChatRoomId(memberId, roomId);
        chatRoom.decrementParticipantCount();
    }

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getMyChatRooms(Long memberId) {
        Member member = memberRepository.findByIdOrElseThrow(memberId);
        List<ChatRoom> chatRooms = memberChatRoomRepository.findChatRoomsByMember(member);

        return chatRooms.stream()
                .map(chatRoom -> ChatRoomResponse.from(chatRoom))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomParticipantsResponse getChatRoomParticipants(Long roomId) {
        if (!chatRoomRepository.existsById(roomId)) {
            throw new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND);
        }

        List<MemberChatRoom> memberChatRooms = memberChatRoomRepository.findAllByChatRoomId(roomId);
        List<ParticipantInfo> participants = memberChatRooms.stream()
                .map(memberChatRoom -> ParticipantInfo.from(memberChatRoom.getMember()))
                .toList();

        return new ChatRoomParticipantsResponse(participants.size(), participants);
    }
} 
