package com.nhnacademy.common.advice;

import com.nhnacademy.common.exception.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import org.springframework.validation.BindException; // spring의 BindException으로 수정

/**
 * 전역 예외 처리 클래스입니다.
 * REST 컨트롤러에서 발생하는 예외들을 공통적으로 처리하며,
 * 예외 종류에 따라 적절한 HTTP 응답을 반환합니다.
 */
@RestControllerAdvice
public class CommonAdvice {

    /**
     * 요청 파라미터 바인딩 과정에서 발생하는 예외를 처리합니다.
     *
     * @return HTTP 400 Bad Request와 함께 "BAD REQUEST" 메시지를 반환합니다.
     */
    @ExceptionHandler(BindException.class)
    public ResponseEntity<String> bindExceptionHandler(){
        return ResponseEntity
                .badRequest()
                .body("BAD REQUEST");
    }

    /**
     * 리소스를 찾을 수 없을 때 발생하는 NotFoundException을 처리합니다.
     *
     * @return HTTP 404 Not Found와 함께 "NOT FOUND EXCEPTION HANDLER" 메시지를 반환합니다.
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<String> NotFoundExceptionHandler(){
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body("NOT FOUND EXCEPTION HANDLER");
    }

    /**
     * 위에서 처리되지 않은 일반적인 예외(Exception)를 처리합니다.
     * 대부분의 일반적인 예외 처리용
     *
     * @param ex 발생한 예외 객체
     * @return HTTP 500 Internal Server Error와 함께 예외 메시지를 반환합니다.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex){
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Server Error: " + ex.getMessage());
    }

    /**
     * Throwable을 처리하는 최후의 예외 처리자입니다.
     * Exception으로 처리되지 않는 Error 등 모든 예외를 포괄합니다.
     *
     * @return HTTP 500 Internal Server Error와 함께 "THROWABLE" 메시지를 반환합니다.
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<String> exceptionHandler(){
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("THROWABLE");
    }
}