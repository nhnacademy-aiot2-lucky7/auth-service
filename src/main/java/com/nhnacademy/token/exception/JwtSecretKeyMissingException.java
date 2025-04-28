package com.nhnacademy.token.exception;

public class JwtSecretKeyMissingException extends TokenException{
    private static final String MESSAGE = "JWT_SECRET가 설정되지 않았습니다.";

    public JwtSecretKeyMissingException(){
        super(MESSAGE);
    }

    public JwtSecretKeyMissingException(String message) {
        super(message);
    }

    public JwtSecretKeyMissingException(String message, Throwable cause) {
        super(message, cause);
    }
}
