package server.challenge.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CountType {
    VIEW("view"),
    LIKE("like"),
    COMMENT("comment");

    private final String hashKey;
} 
