package server.chat.dto;

import static book.book.chat.domain.QChatRoom.chatRoom;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;

@Getter
@RequiredArgsConstructor
public enum ChatRoomSort {
    POPULAR("participantCount") {
        @Override
        public OrderSpecifier<?>[] getOrderSpecifiers() {
            List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, chatRoom.participantCount));
            orderSpecifiers.add(new OrderSpecifier<>(Order.DESC, chatRoom.id)); // 2차 정렬
            return orderSpecifiers.toArray(new OrderSpecifier[0]);
        }
    };

    private final String property;

    public abstract OrderSpecifier<?>[] getOrderSpecifiers();
} 
