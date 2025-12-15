package kr.co.inter_musica.exception;

import kr.co.inter_musica.dto.ApiError;
import kr.co.inter_musica.dto.ApiErrorResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 프로젝트 내에서 발생할 수 있는 예외를 예상해서? AppException 으로 묶어서 처리
    // GlobalExceptionHandler 에서 줄줄이 처리하는 것이 지저분하다고 생각했다
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiErrorResponse> handleAppException(AppException e) {
        ErrorCode errorCode = e.getErrorCode();

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(
                        new ApiErrorResponse(
                                new ApiError(errorCode.getCode(), errorCode.getMessage())
                        )
                );
    }

    // 내가 아는 예외를 제외하면 500 으로 돌리는게 맞나? 실무에서도 이렇게 작성 후 로그로 디버그 하는 듯
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnknownException(Exception e) {
        return ResponseEntity
                .status(500)
                .body(
                        new ApiErrorResponse(
                                new ApiError("INTERNAL_ERROR", "서버 오류")
                        )
                );
    }
}
