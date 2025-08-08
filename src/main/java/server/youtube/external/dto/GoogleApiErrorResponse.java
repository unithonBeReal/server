package server.youtube.external.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleApiErrorResponse {

    @JsonProperty("error")
    private ErrorDetails error;

    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorDetails {
        @JsonProperty("code")
        private int code;

        @JsonProperty("message")
        private String message;

        @JsonProperty("errors")
        private List<ErrorItem> errors;
    }

    @Getter
    @ToString
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ErrorItem {
        @JsonProperty("message")
        private String message;

        @JsonProperty("domain")
        private String domain;

        @JsonProperty("reason")
        private String reason;
    }
} 
