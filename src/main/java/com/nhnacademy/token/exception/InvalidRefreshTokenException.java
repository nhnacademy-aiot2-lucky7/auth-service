package com.nhnacademy.token.exception;

public class InvalidRefreshTokenException extends TokenException{
    public InvalidRefreshTokenException(){
        super("저장된 refresh token이 유효하지 않습니다.");
    }

    public InvalidRefreshTokenException(String message) {
        super(message);
    }

    public InvalidRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
