package com.nhnacademy.common.exception;

public class FailSignUpException extends CommonHttpException{

    public FailSignUpException(int statusCode, String message) {
        super(statusCode, message);
    }

    public FailSignUpException(int statusCode){
        super(statusCode, "회원가입에 실패하였습니다.");
    }
}
