package com.nhnacademy.token.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * AccessTokenResponse 클래스는 액세스 토큰과 해당 토큰의 TTL(Time To Live)을 포함하는 응답 객체입니다.
 * <p>
 * 이 객체는 액세스 토큰이 재발급되었을 때 클라이언트에게 반환됩니다.
 * {@code accessToken} 필드는 재발급된 액세스 토큰을 나타내며, {@code ttl} 필드는
 * 액세스 토큰의 만료 시간을 밀리초 단위로 나타냅니다.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public class AccessTokenResponse {
    /**
     * 재발급된 액세스 토큰.
     * <p>
     * 클라이언트에게 제공되는 새로운 액세스 토큰입니다.
     * </p>
     */
    private final String accessToken;

    /**
     * 액세스 토큰의 TTL(Time To Live) 값, 즉 토큰의 만료 시간을 밀리초 단위로 나타냅니다.
     * <p>
     * 이 값은 토큰의 유효 기간을 나타내며, 토큰이 만료되기까지의 시간을 클라이언트에 전달합니다.
     * </p>
     */
    private final long ttl;
}
