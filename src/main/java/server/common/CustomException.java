package server.common;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorCode errorCode;

    /**
     * 평상시에는 이 방식 사용
     */
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    /**
     * 예기치 못한 에러이고 정확한 에러 보고 싶을 때 사용
     * ex) 외부 API 호출 예외처리
     */
    public CustomException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public CustomException(ErrorCode errorCode, Throwable cause) {
        super(cause);
        this.errorCode = errorCode;
    }
}
