package server.veo.dto;

/**
 * 비디오 생성 작업의 상태를 나타내는 열거형
 */
public enum VideoGenerationStatus {
    /**
     * 작업이 진행 중인 상태
     */
    IN_PROGRESS("작업 진행 중"),

    /**
     * 작업이 성공적으로 완료된 상태
     */
    COMPLETED("작업 완료"),

    /**
     * 작업이 실패한 상태
     */
    FAILED("작업 실패");

    private final String description;

    VideoGenerationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 