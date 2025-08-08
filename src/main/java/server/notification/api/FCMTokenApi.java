package server.notification.api;

import server.notification.dto.FCMTokenCreateRequest;
import server.notification.service.FCMService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "notification")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/notifications")
public class FCMTokenApi {

    private final FCMService fcmService;

    @PostMapping("/fcmToken")
    @Operation(summary = "FCM 토큰 저장 API")
    public void save(@RequestBody FCMTokenCreateRequest request) {
        fcmService.save(request);
    }
}
