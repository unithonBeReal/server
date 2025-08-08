package server.notification.dto;

import server.notification.domain.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotificationMessage {
    private Long receiverId;
    private String title;
    private String content;
    private NotificationType notificationType;
}
