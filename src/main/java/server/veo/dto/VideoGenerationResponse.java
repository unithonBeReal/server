package server.veo.dto;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 비디오 생성 응답 DTO
 * 
 * 비디오 생성 작업의 결과를 클라이언트에게 전달하는 클래스입니다.
 */
public record VideoGenerationResponse(
        String operationId,
        VideoGenerationStatus status,
        String promt,
        GeneratedVideo video,
        String errorMessage,
        LocalDateTime createdAt,
        LocalDateTime completedAt

) {

    /**
     * 완료된 작업 응답 생성
     */
    public static VideoGenerationResponse completed(String operationId, String prompt, GeneratedVideo video) {

        return new VideoGenerationResponse(
                operationId,
                VideoGenerationStatus.COMPLETED,
                prompt,
                video, // ✅ null 안전성 보장
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
} 
