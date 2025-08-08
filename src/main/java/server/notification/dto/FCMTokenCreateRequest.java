package server.notification.dto;

public record FCMTokenCreateRequest(Long userId, String fcmToken) {
}
