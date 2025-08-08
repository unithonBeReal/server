package server.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record NotificationUploadRequest(
        @NotBlank
        Long userId,
        @NotBlank
        String title,
        @NotBlank
        String content
) {
}

