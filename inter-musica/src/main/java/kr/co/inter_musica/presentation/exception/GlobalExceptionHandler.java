package kr.co.inter_musica.presentation.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e, HttpServletRequest req) {
        ErrorCode code = e.errorCode();
        return ResponseEntity
                .status(code.status())
                .body(ErrorResponse.of(code, e.getMessage(), req.getRequestURI()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception e, HttpServletRequest req) {
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.status())
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, "요청 값이 올바르지 않습니다.", req.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e, HttpServletRequest req) {
        return ResponseEntity
                .status(ErrorCode.FORBIDDEN.status())
                .body(ErrorResponse.of(ErrorCode.FORBIDDEN, "접근 권한이 없습니다.", req.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception e, HttpServletRequest req) {
        return ResponseEntity
                .status(ErrorCode.INTERNAL_ERROR.status())
                .body(ErrorResponse.of(ErrorCode.INTERNAL_ERROR, "서버 오류가 발생했습니다.", req.getRequestURI()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException e, HttpServletRequest req) {
        return ResponseEntity
                .status(ErrorCode.INVALID_REQUEST.status())
                .body(ErrorResponse.of(ErrorCode.INVALID_REQUEST, e.getMessage(), req.getRequestURI()));
    }
}
