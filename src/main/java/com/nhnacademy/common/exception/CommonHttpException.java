package com.nhnacademy.common.exception;

/**
 * HTTP 오류를 처리하는 기본 예외 클래스입니다.
 * <p>
 * 이 클래스는 상태 코드와 오류 메시지를 포함하여 HTTP 오류와 관련된 예외를 처리하는 데 사용됩니다.
 * </p>
 */
public class CommonHttpException extends RuntimeException {

    private final int statusCode;

    /**
     * 주어진 상태 코드와 메시지로 {@link CommonHttpException} 예외를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    오류 메시지
     */
    public CommonHttpException(final int statusCode, final String message) {
        super(message);
        this.statusCode = statusCode;
    }

    /**
     * 주어진 상태 코드, 메시지 및 원인으로 {@link CommonHttpException} 예외를 생성합니다.
     *
     * @param statusCode HTTP 상태 코드
     * @param message    오류 메시지
     * @param cause      예외의 원인
     */
    public CommonHttpException(final int statusCode, final String message, final Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    /**
     * 예외와 함께 HTTP 상태 코드를 반환합니다.
     *
     * @return HTTP 상태 코드
     */
    public int getStatusCode() {
        return statusCode;
    }

}
