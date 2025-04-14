package com.nhnacademy.common.exception;

/**
 * HTTP 401 Unauthorized 오류를 처리하는 예외 클래스입니다.
 * <p>
 * 이 예외는 인증되지 않은 요청이나 잘못된 인증 정보로 인해 발생하는 오류를 처리하기 위해 사용됩니다.
 * </p>
 */
public class UnauthorizedException extends CommonHttpException {
    private static final int HTTP_STATUS_CODE = 401;

    /**
     * 기본 메시지("Unauthorized")와 함께 {@link UnauthorizedException} 예외를 생성합니다.
     */
    public UnauthorizedException() {
        super(HTTP_STATUS_CODE, "Unauthorized");
    }

    /**
     * 주어진 메시지와 함께 {@link UnauthorizedException} 예외를 생성합니다.
     *
     * @param message 예외 메시지
     */
    public UnauthorizedException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}
