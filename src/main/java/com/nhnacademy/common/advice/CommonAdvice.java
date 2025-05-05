package com.nhnacademy.common.advice;

import com.nhnacademy.common.exception.CommonHttpException;
import com.nhnacademy.token.exception.TokenException;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 전역 예외를 처리하는 핸들러 클래스입니다.
 * <p>
 * Feign 오류, 바인딩 오류, 커스텀 예외 등을 통합 처리합니다.
 */
@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    /**
     * FeignException 처리 핸들러
     *
     * @param ex FeignException
     * @return 502 또는 404 응답
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<String> handleFeignException(FeignException ex) {
        log.error("Feign 호출 예외 발생: status={}, message={}", ex.status(), ex.getMessage(), ex);

        if (ex.status() == 404) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("요청한 자원을 찾을 수 없습니다.");
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body("외부 서비스 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    /**
     * Feign 연결 재시도 예외 처리
     *
     * @param ex RetryableException
     * @return 503 응답
     */
    @ExceptionHandler(RetryableException.class)
    public ResponseEntity<String> handleRetryableException(RetryableException ex) {
        log.error("외부 서비스 연결 실패: {}", ex.getMessage(), ex);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("일시적인 연결 문제입니다. 잠시 후 다시 시도해주세요.");
    }

    /**
     * Request 바인딩 오류 처리
     *
     * @param e BindException
     * @return 400 응답
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> bindExceptionHandler(BindException e) {
        log.warn("요청 필드 바인딩 오류 발생: {}", e.getMessage());

        StringBuilder errorMessage = new StringBuilder("요청에 문제가 있습니다: ");
        for (FieldError fieldError : e.getFieldErrors()) {
            errorMessage.append(fieldError.getField())
                    .append(" - ")
                    .append(fieldError.getDefaultMessage())
                    .append("; ");
        }

        return ResponseEntity
                .badRequest()
                .body(errorMessage.toString());
    }

    /**
     * 사용자 정의 HTTP 예외 처리
     *
     * @param e CommonHttpException
     * @return 지정된 상태 코드와 메시지
     */
    @ExceptionHandler(CommonHttpException.class)
    public ResponseEntity<String> commonExceptionHandler(CommonHttpException e) {
        log.warn("CommonHttpException 발생: {}", e.getMessage());
        return ResponseEntity
                .status(e.getStatusCode())
                .body(e.getMessage());
    }

    /**
     * JWT 등 인증 관련 예외 처리
     *
     * @param e TokenException
     * @return 500 응답
     */
    @ExceptionHandler(TokenException.class)
    public ResponseEntity<String> tokenExceptionHandler(TokenException e) {
        log.warn("TokenException 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("인증 처리 중 오류가 발생했습니다.");
    }

    /**
     * 처리되지 않은 모든 예외 처리
     *
     * @param e Throwable
     * @return 500 응답
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> exceptionHandler(Throwable e) {
        log.error("Unhandled 예외 발생: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("내부 서버 오류가 발생했습니다.");
    }
}
