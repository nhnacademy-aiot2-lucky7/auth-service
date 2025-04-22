package com.nhnacademy.common.exception;

/**
 * {@code InvalidRedisConfigException}은 Redis 설정값이 유효하지 않거나 누락되었을 때 발생하는 예외입니다.
 *
 * <p>예를 들어, 포트 값이 숫자가 아니거나 필수 설정값이 존재하지 않는 경우 등에 사용됩니다.</p>
 *
 * <p>이 예외는 {@link RuntimeException}을 상속하며, 일반적으로 개발자가 Redis 설정을 확인하고 수정해야 함을 알립니다.</p>
 *
 * <pre>{@code
 * throw new InvalidRedisConfigException("Invalid redis port: not a number");
 * }</pre>
 *
 */
public class InvalidRedisConfigException extends RuntimeException {

    /**
     * 상세 메시지를 포함하는 {@code InvalidRedisConfigException}을 생성합니다.
     *
     * @param message 예외의 상세 설명 메시지
     */
    public InvalidRedisConfigException(String message) {
        super(message);
    }
}
