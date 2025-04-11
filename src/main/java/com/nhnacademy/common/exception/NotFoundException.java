package com.nhnacademy.common.exception;

/**
 * 요청한 리소스를 찾을 수 없을 때 발생하는 예외 클래스입니다.
 * <p>
 * HTTP 상태 코드 404 (Not Found)를 나타냅니다.
 * 예: 존재하지 않는 사용자, 게시글, 파일 등에 접근할 경우 사용됩니다.
 */
public class NotFoundException extends CommonHttpException {

  /**
   * HTTP 상태 코드 404 (Not Found)
   */
  private static final int STATUS_CODE = 404;

  /**
   * 사용자 정의 메시지를 포함한 NotFound 예외를 생성합니다.
   *
   * @param message 예외 메시지
   */
  public NotFoundException(String message) {
    super(STATUS_CODE, message);
  }

  /**
   * 기본 메시지("NOT FOUND EXCEPTION!!!")를 포함한 NotFound 예외를 생성합니다.
   */
  public NotFoundException() {
    super(STATUS_CODE, "NOT FOUND EXCEPTION!!!");
  }
}