package server.chat.dto.response;

import java.util.List;

public record ChatRoomParticipantsResponse(
    int totalParticipantCount,
    List<ParticipantInfo> participants
) {
} 
