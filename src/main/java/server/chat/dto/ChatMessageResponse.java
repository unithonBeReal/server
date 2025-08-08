package server.chat.dto;

import server.chat.domain.ChatMessage;
import server.chat.domain.MessageType;
import java.time.format.DateTimeFormatter;
import lombok.Builder;

@Builder
public record ChatMessageResponse(
        Long id,
        Long senderId,
        String senderName,
        String content,
        MessageType messageType,
        String fileUrl,
        String createdAt
) {
    public static ChatMessageResponse from(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderName(message.getSender().getNickName())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .fileUrl(message.getFileUrl())
                .createdAt(message.getCreatedDate() == null ? null
                        : message.getCreatedDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build();
    }
} 
