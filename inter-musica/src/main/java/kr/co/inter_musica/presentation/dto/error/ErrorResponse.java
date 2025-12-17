package kr.co.inter_musica.presentation.dto.error;

import kr.co.inter_musica.domain.enums.ErrorCode;

import java.time.Instant;

public class ErrorResponse {

    private String errorCode;
    private String message;
    private String path;
    private Instant timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String errorCode, String message, String path, Instant timestamp) {
        this.errorCode = errorCode;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(errorCode.getMessage(), message, path, Instant.now());
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
