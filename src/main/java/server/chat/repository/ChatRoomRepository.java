package server.chat.repository;

import server.chat.domain.ChatRoom;
import server.common.CustomException;
import server.common.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    default ChatRoom findByIdOrElseThrow(Long id) {
        return findById(id).orElseThrow(() -> new CustomException(ErrorCode.CHAT_ROOM_NOT_FOUND));
    }
} 
