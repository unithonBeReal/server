package server.chat.repository;

import static book.book.chat.domain.QChatMessage.chatMessage;

import server.chat.domain.ChatMessage;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<ChatMessage> findMessagesByRoom(Long roomId, Long cursorId, int pageSize) {
        return queryFactory
                .selectFrom(chatMessage)
                .where(
                        chatMessage.chatRoom.id.eq(roomId),
                        loeCursorId(cursorId)
                )
                .orderBy(chatMessage.id.desc())
                .limit(pageSize)
                .fetch();
    }

    private BooleanExpression loeCursorId(Long cursorId) {
        return cursorId == null ? null : chatMessage.id.loe(cursorId);
    }
} 
