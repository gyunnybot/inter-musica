package kr.co.inter_musica.presentation.exception;

import java.time.Instant;

public class ErrorResponse {

    private String code;
    private String message;
    private String path;
    private Instant timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(String code, String message, String path, Instant timestamp) {
        this.code = code;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, String path) {
        return new ErrorResponse(errorCode.code(), message, path, Instant.now());
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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
