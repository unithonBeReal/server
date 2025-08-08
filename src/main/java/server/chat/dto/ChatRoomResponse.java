package server.chat.dto;

import server.chat.domain.ChatCategory;
import server.chat.domain.ChatRoom;
import lombok.Builder;

@Builder
public record ChatRoomResponse(
        Long id,
        String name,
        ChatCategory category,
        int participantCount) {
    public static ChatRoomResponse from(ChatRoom chatRoom) {
        return ChatRoomResponse.builder()
                .id(chatRoom.getId())
                .name(chatRoom.getName())
                .category(chatRoom.getCategory())
                .participantCount(chatRoom.getParticipantCount())
                .build();
    }
} 
