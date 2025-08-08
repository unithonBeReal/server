package server.notification.domain;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum NotificationType {
    COMMENT("comment"),
    REPLY("reply"),
    LIKE("like"),
    FOLLOW("follow"),
    CHAT("chat");

    private final String type;
}
