package com.nhnacademy.common.exception;

/**
 * 리소스를 찾을 수 없을 때 발생하는 예외 클래스입니다.
 * <p>
 * 이 예외는 HTTP 상태 코드 404 (Not Found)를 나타내며, 요청한 리소스가 서버에 존재하지 않는 경우 사용됩니다.
 * </p>
 */
public class NotFoundException extends CommonHttpException {

  /**
   * HTTP 상태 코드 404 (Not Found)
   */
  private static final int STATUS_CODE = 404;

  /**
   * 주어진 메시지와 함께 {@link NotFoundException} 예외를 생성합니다.
   *
   * @param message 리소스를 찾을 수 없다는 설명
   */
  public NotFoundException(String message) {
    super(STATUS_CODE, message);
  }
}
