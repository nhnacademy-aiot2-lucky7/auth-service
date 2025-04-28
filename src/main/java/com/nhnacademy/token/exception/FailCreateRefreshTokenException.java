package com.nhnacademy.token.exception;

public class FailCreateRefreshTokenException extends TokenException{
    private static final String MESSAGE = "RefreshToken 생성 중 예외 발생";

    public FailCreateRefreshTokenException() {
        super(MESSAGE);
    }

    public FailCreateRefreshTokenException(String message) {
        super(message);
    }

    public FailCreateRefreshTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
