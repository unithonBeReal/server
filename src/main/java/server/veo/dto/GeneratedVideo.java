package server.veo.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 생성된 비디오 정보를 담는 DTO
 */
@Builder
public record GeneratedVideo(
        String id,
        String url,
        String status,
        LocalDateTime createdAt,
        String mimeType,
        Integer durationSeconds,
        Long fileSizeBytes,
        String thumbnailUrl
) {
    /**
     * 기본 비디오 정보로 생성하는 정적 팩토리 메서드
     */
    public static GeneratedVideo create(String videoId, String downloadUrl, Integer durationSeconds) {
        return GeneratedVideo.builder()
                .id(videoId)
                .url(downloadUrl)
                .status("SUCCESS")
                .createdAt(LocalDateTime.now())
                .mimeType("video/mp4")
                .durationSeconds(durationSeconds)
                .fileSizeBytes(null)
                .thumbnailUrl(null)
                .build();
    }
} 
