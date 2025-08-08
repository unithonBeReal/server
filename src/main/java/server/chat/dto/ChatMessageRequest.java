package server.chat.dto;

import server.chat.domain.ChatMessage;
import server.chat.domain.ChatRoom;
import server.chat.domain.MessageType;
import server.member.entity.Member;
import jakarta.validation.constraints.NotNull;

public record ChatMessageRequest(
        String content,
        @NotNull
        MessageType messageType,
        String fileUrl
) {
    public ChatMessage toEntity(Member sender, ChatRoom chatRoom) {
        return ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .content(content)
                .messageType(messageType)
                .fileUrl(fileUrl)
                .build();
    }
} 
