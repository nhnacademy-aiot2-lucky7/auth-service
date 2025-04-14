package com.nhnacademy.common.exception;

public class FailSignInException extends CommonHttpException{

    public FailSignInException(int statusCode, String message) {
        super(statusCode, message);
    }

    public FailSignInException(int statusCode, String message, Throwable cause) {
        super(statusCode, message, cause);
    }

    public FailSignInException(int statusCode){
        super(statusCode, "SignIn FAIL!!!");
    }
}
