package com.lifelogix.exception;

import com.lifelogix.common.ErrorResponse; // common 패키지의 ErrorResponse 사용
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 직접 정의한 비즈니스 예외 처리
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                errorCode.getStatus().value(),
                errorCode.getStatus().getReasonPhrase(),
                errorCode.getMessage(),
                request.getRequestURI()
        );
        log.warn("BusinessException Occurred: {}", errorResponse);
        return new ResponseEntity<>(errorResponse, errorCode.getStatus());
    }

    // 처리하지 못한 모든 예외에 대한 최종 처리 (500 Internal Server Error)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, HttpServletRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                500,
                "Internal Server Error",
                "서버 내부 오류가 발생했습니다. 관리자에게 문의해주세요.",
                request.getRequestURI()
        );
        log.error("Unhandled Exception Occurred:", ex); // 예상치 못한 오류는 error 레벨로 로그 기록
        return new ResponseEntity<>(errorResponse, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}