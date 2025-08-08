package server.common.response;

import server.common.ErrorCode;
import lombok.Getter;

@Getter
public class StatusResponse {
    private final String resultCode;
    private final String resultMessage;

    public StatusResponse(ErrorCode errorCode) {
        this.resultCode = errorCode.getCode();
        this.resultMessage = errorCode.getMessage();
    }

    public StatusResponse(ErrorCode errorCode, String message) {
        this.resultCode = errorCode.getCode();
        this.resultMessage = message;
    }
}
