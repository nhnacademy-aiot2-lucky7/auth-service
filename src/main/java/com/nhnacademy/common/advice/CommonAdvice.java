package com.nhnacademy.common.advice;

import com.nhnacademy.common.exception.CommonHttpException;
import com.nhnacademy.common.exception.FailSignInException;
import com.nhnacademy.common.exception.FailSignUpException;
import com.nhnacademy.common.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.validation.BindException; // spring의 BindException으로 수정

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * {@code @RestControllerAdvice}를 사용한 전역 예외 처리 클래스입니다.
 * REST 컨트롤러 전반에서 발생하는 다양한 예외를 공통적으로 처리하며,
 * 각 예외 유형에 따라 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 */
@Slf4j
@RestControllerAdvice
public class CommonAdvice {

    /**
     * {@link BindException} 발생 시 처리합니다.
     * <p>
     * 요청 파라미터 검증 과정에서 발생한 바인딩 예외를 처리하며,
     * 필드별 에러 메시지를 포함한 상세한 응답을 제공합니다.
     *
     * @param e 바인딩 예외 객체
     * @return 400 Bad Request 상태와 상세 에러 메시지를 포함한 응답
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> bindExceptionHandler(BindException e){
        log.warn("BindException 발생: {}", e.getMessage());

        StringBuilder errorMessage = new StringBuilder("Bad Request: ");

        for(FieldError fieldError: e.getFieldErrors()){
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
     * {@link CommonHttpException} 및 이를 상속하는 예외 처리.
     * <p>
     * 커스텀 HTTP 예외 발생 시 400 상태 코드와 메시지를 반환합니다.
     *
     * @param e 공통 HTTP 예외 객체
     * @return 400 Bad Request 상태와 예외 메시지를 포함한 응답
     */
    @ExceptionHandler(CommonHttpException.class)
    public ResponseEntity<String> commonExceptionHandler(CommonHttpException e){
        log.warn("CommonHttpException 발생: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("CommonException: "+e.getMessage());
    }

    /**
     * {@link FailSignUpException} 발생 시 처리.
     * <p>
     * 회원가입 실패 예외 발생 시 예외 내부의 상태 코드와 메시지를 기반으로 응답합니다.
     *
     * @param ex 회원가입 실패 예외
     * @return 예외에 정의된 HTTP 상태 코드와 메시지를 포함한 응답
     */
    @ExceptionHandler(FailSignUpException.class)
    public ResponseEntity<String> failSignUpExceptionHandler(FailSignUpException ex){
        log.warn("FailSignUpException 발생: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body("FailSignUpException EXCEPTION HANDLER: "+ex.getMessage());
    }

    /**
     * {@link FailSignInException} 발생 시 처리.
     * <p>
     * 로그인 실패 예외 발생 시 예외 내부의 상태 코드와 메시지를 기반으로 응답합니다.
     *
     * @param ex 로그인 실패 예외
     * @return 예외에 정의된 HTTP 상태 코드와 메시지를 포함한 응답
     */
    @ExceptionHandler(FailSignInException.class)
    public ResponseEntity<String> failSignInExceptionHandler(FailSignInException ex){
        log.warn("FailSignInException 발생: {}", ex.getMessage());
        return ResponseEntity
                .status(ex.getStatusCode())
                .body("FailSignInException EXCEPTION HANDLER: "+ex.getMessage());
    }

    /**
     * {@link NotFoundException} 발생 시 처리.
     * <p>
     * 리소스를 찾지 못했을 때 404 상태 코드와 메시지를 반환합니다.
     *
     * @param ex NotFoundException 예외
     * @return 404 Not Found 상태와 예외 메시지를 포함한 응답
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> notFoundExceptionHandler(NotFoundException ex){
        log.warn("NotFoundException 발생: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("NOT FOUND EXCEPTION HANDLER: "+ex.getMessage());
    }

    /**
     * 처리되지 않은 일반 {@link Exception}을 포괄적으로 처리합니다.
     * <p>
     * 대부분의 예외 상황에 대해 500 상태 코드와 메시지를 반환합니다.
     *
     * @param ex 발생한 일반 예외
     * @return 500 Internal Server Error 상태와 예외 메시지를 포함한 응답
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex){
        log.warn("Exception 발생: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Server Error: " + ex.getMessage());
    }

    /**
     * {@link Throwable}을 처리하는 최후의 예외 처리자입니다.
     * <p>
     * Exception으로도 처리되지 않는 {@code Error} 등의 모든 예외를 포괄합니다.
     *
     * @param e 발생한 Throwable
     * @return 500 Internal Server Error 상태와 메시지를 포함한 응답
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> exceptionHandler(Throwable e){
        log.warn("Throwable 발생: {}", e.getMessage());

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stackTrace = sw.toString();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Internal Server Error 발생\n" + stackTrace);
    }
}