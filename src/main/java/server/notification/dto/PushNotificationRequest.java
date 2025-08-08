package server.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushNotificationRequest {
    private String title;
    private String body;
    private String sound = "default";
    private Integer badge;
}
