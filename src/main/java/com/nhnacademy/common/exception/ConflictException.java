package com.nhnacademy.common.exception;

/**
 * 리소스와의 충돌을 나타내는 예외 클래스입니다.
 * <p>
 * 이 예외는 HTTP 상태 코드 409 (Conflict)를 나타내며, 주로 이미 존재하는 리소스와의 충돌이 발생했을 때 사용됩니다.
 * </p>
 */
public class ConflictException extends CommonHttpException {
    private static final int HTTP_STATUS_CODE = 409;

    /**
     * 기본 메시지("Conflict with existing resource")와 함께 {@link ConflictException} 예외를 생성합니다.
     */
    public ConflictException() {
        super(HTTP_STATUS_CODE, "Conflict with existing resource");
    }

    /**
     * 주어진 메시지와 함께 {@link ConflictException} 예외를 생성합니다.
     *
     * @param message 충돌에 대한 상세 설명
     */
    public ConflictException(String message) {
        super(HTTP_STATUS_CODE, message);
    }
}
