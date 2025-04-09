package com.nhnacademy.common.exception;

public class NotFoundException extends CommonHttpException {

  private static final int STATUS_CODE = 404;
  public NotFoundException(String message) {
    super(STATUS_CODE, message);
  }

  public NotFoundException(){
    super(STATUS_CODE, "NOT FOUND EXCEPTION!!!");
  }
}
