package kr.co.inter_musica.exception;

import kr.co.inter_musica.dto.ApiError;
import kr.co.inter_musica.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException ex) {
        ErrorCode ec = ex.getErrorCode();
        return ResponseEntity.status(ec.getStatus())
                .body(new ApiErrorResponse(new ApiError(ec.getCode(), ec.getMessage())));
    }

    // 혹시 몰라서 정의?
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknown(Exception ex) {
        return ResponseEntity.status(500)
                .body(new ApiErrorResponse(new ApiError("INTERNAL_ERROR", "서버 오류")));
    }
}
