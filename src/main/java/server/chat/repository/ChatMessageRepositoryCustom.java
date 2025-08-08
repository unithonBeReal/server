package server.chat.repository;

import server.chat.domain.ChatMessage;
import java.util.List;

public interface ChatMessageRepositoryCustom {

    List<ChatMessage> findMessagesByRoom(Long roomId, Long cursorId, int pageSize);
}
