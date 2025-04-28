package com.nhnacademy.token.exception;

public class FailCreateAccessTokenException extends TokenException{
    private static final String MESSAGE = "AccessToken 생성 중 예외 발생";

    public FailCreateAccessTokenException() {
        super(MESSAGE);
    }

    public FailCreateAccessTokenException(String message) {
        super(message);
    }

    public FailCreateAccessTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
