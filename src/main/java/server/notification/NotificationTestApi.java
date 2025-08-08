package server.notification;

import server.notification.dto.NotificationMessage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notification")
@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationTestApi {

    private final ApplicationEventPublisher publisher;

    @PostMapping("/notification/test")
    @Operation(summary = "알림 테스트용도 API")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationMessage message) {
        try {
            log.info("알림 전달 메시지 : {}", message);
            publisher.publishEvent(message);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("알림 전달 실패!!: ", e);
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
