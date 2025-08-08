package server.chat.event.dto;

/**
 * '채팅 메시지' 전송 이벤트
 *
 * @param chatRoomId 메시지가 전송된 채팅방 ID
 * @param senderId   메시지를 보낸 사용자 ID
 * @param message    메시지 내용
 */
public record ChatMessageSentEvent(
        Long chatRoomId,
        Long senderId,
        String message
) {
} 
