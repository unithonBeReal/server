package server.chat.service;

import server.chat.ably.service.AblyService;
import server.chat.domain.ChatMessage;
import server.chat.domain.ChatRoom;
import server.chat.dto.ChatMessageRequest;
import server.chat.dto.ChatMessageResponse;
import server.chat.event.dto.ChatMessageSentEvent;
import server.chat.repository.ChatMessageRepository;
import server.chat.repository.ChatRoomRepository;
import server.common.response.CursorPageResponse;
import server.member.entity.Member;
import server.member.repository.MemberRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final AblyService ablyService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ChatMessageResponse saveMessage(Long roomId, Long memberId, ChatMessageRequest request) {
        final Member sender = memberRepository.findByIdOrElseThrow(memberId);
        final ChatRoom chatRoom = chatRoomRepository.findByIdOrElseThrow(roomId);

        final ChatMessage chatMessage = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(sender)
                .messageType(request.messageType())
                .fileUrl(request.fileUrl())
                .content(request.content())
                .build();

        final ChatMessage savedChatMessage = chatMessageRepository.save(chatMessage);

        final ChatMessageResponse response = ChatMessageResponse.from(savedChatMessage);
        // TODO: 프론트분과 상의
//        ablyService.publish(roomId, response);

        eventPublisher.publishEvent(
                new ChatMessageSentEvent(roomId, memberId, request.content())
        );

        return response;
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ChatMessageResponse> getChatHistory(Long roomId, Long cursorId, int size) {
        List<ChatMessage> messages = chatMessageRepository.findMessagesByRoom(roomId, cursorId, size+1);
        List<ChatMessageResponse> messageResponses = messages.stream()
                .map(ChatMessageResponse::from)
                .toList();

        return CursorPageResponse.of(messageResponses, size, ChatMessageResponse::id);
    }
} 
