package com.lifelogix.exception;

import com.lifelogix.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ResponseStatus 어노테이션이 붙은 모든 RuntimeException을 처리
     **/
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex, HttpServletRequest request) {
        // 1. 발생한 예외에서 @ResponseStatus 어노테이션을 체크
        ResponseStatus responseStatus = AnnotationUtils.findAnnotation(ex.getClass(), ResponseStatus.class);

        HttpStatus status;
        String reason;

        // 2. @ResponseStatus가 존재하면 해당 값을 사용
        if (responseStatus != null) {
            status = responseStatus.code();
            reason = responseStatus.reason();
        } else {
            // 3. @ResponseStatus가 없으면 500 Internal Server Error를 기본값으로 사용
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            reason = status.getReasonPhrase();
        }

        // 4. @ResponseStatus에 reason이 명시되지 않은 경우, HttpStatus의 기본 메시지를 사용 (방어 코드)
        if (reason.isEmpty()) {
            reason = status.getReasonPhrase();
        }

        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                reason,
                ex.getMessage(),
                request.getRequestURI()
        );

        return new ResponseEntity<>(errorResponse, status);
    }
}
