package com.lifelogix.exception;

import com.lifelogix.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * @RestControllerAdvice: 모든 @RestController에서 발생하는 예외를 전역적으로 처리
 **/
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * @ExceptionHandler: 특정 예외(IllegalArgumentException)가 발생했을 때
     **/
    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmailException(DuplicateEmailException ex, HttpServletRequest request) {

        // API 명세에 맞는 에러 응답 객체를 생성
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(), // 409 Conflict 상태 코드
                HttpStatus.CONFLICT.getReasonPhrase(), // "Conflict" 메시지
                ex.getMessage(), // UserService에서 던진 실제 에러 메시지 (e.g., "이미 사용 중인 이메일입니다.")
                request.getRequestURI() // 요청이 발생한 경로
        );

        // 생성된 에러 응답을 ResponseEntity에 담아 409 상태 코드로 클라이언트에게 반환
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {

        // API 명세에 맞는 에러 응답 객체를 생성
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.UNAUTHORIZED.value(), // 401 Unauthorized 상태 코드
                HttpStatus.UNAUTHORIZED.getReasonPhrase(), // "Unauthorized" 메시지
                ex.getMessage(), // UserService에서 던진 실제 에러 메시지 (e.g., "사용자를 찾을 수 없습니다.")
                request.getRequestURI() // 요청이 발생한 경로
        );

        // 생성된 에러 응답을 ResponseEntity에 담아 401 상태 코드로 클라이언트에게 반환
        return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
}
