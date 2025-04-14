package com.nhnacademy.common.exception;

public class FailSignUpException extends CommonHttpException{

    public FailSignUpException(int statusCode, String message) {
        super(statusCode, message);
    }

    public FailSignUpException(int statusCode){
        super(statusCode, "SignUp FAIL!!!!");
    }
}
