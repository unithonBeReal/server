package server.common.response;

import server.common.ErrorCode;
import lombok.Data;

@Data
public class ResponseForm<T> {

    private StatusResponse statusResponse;
    private final T data;

    public ResponseForm() {
        this.data = null;
    }

    /**
     * 요청 성공 시, 응답 dto 객체를 파라미터로 받음
     */
    public ResponseForm(T data) {
        this.data = data;
        this.statusResponse = new StatusResponse(ErrorCode.OK);
    }

    /**
     * 무응답 요청 성공 시
     */
    public static ResponseForm<Void> ok() {
        ResponseForm<Void> responseForm = new ResponseForm<>();
        responseForm.statusResponse = new StatusResponse(ErrorCode.OK);
        return responseForm;
    }

    /**
     * 요청 실패 시, 정의한 에러를 파라미터로 받음
     */
    public ResponseForm(ErrorCode errorCode) {
        this();
        this.statusResponse = new StatusResponse(errorCode);
    }

    public ResponseForm(ErrorCode errorCode, String message) {
        this();
        this.statusResponse = new StatusResponse(errorCode, message);
    }
}
