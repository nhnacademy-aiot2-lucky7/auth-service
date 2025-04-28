package com.nhnacademy.token.exception;

public class RefreshTokenNotFoundException extends TokenException{
    public RefreshTokenNotFoundException(){
        super("저장된 RefreshToken을 찾을 수 없습니다.");
    }

    public RefreshTokenNotFoundException(String message) {
        super(message);
    }

    public RefreshTokenNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
