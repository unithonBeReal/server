package server.chat.repository;

import server.chat.domain.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long>, ChatMessageRepositoryCustom {

    void deleteAllBySenderId(Long memberId);
}
