package com.nhnacademy.token.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

/**
 * RefreshToken 클래스는 사용자 ID와 해당 사용자의 RefreshToken을 Redis에 저장하는 엔티티입니다.
 * <p>
 * 이 클래스는 Spring Data Redis를 사용하여 Redis에 데이터를 저장하며,
 * 사용자의 refreshToken을 관리합니다.
 * </p>
 */
@RedisHash(value = "refreshToken", timeToLive = 7 * 24 * 3600) // 7일
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken implements Serializable {

    /**
     * 사용자 ID로, Redis 저장소에서 해당 사용자에 대한 refreshToken을 찾을 때 사용됩니다.
     * @Id 어노테이션을 통해 Redis에서의 키 역할을 합니다.
     */
    // Redis 저장소에서 데이터를 찾을 키로 'userId' 사용 (pk를 사용시 가장 좋지만 userId or userNo로 통일해야 하는 문제 발생)
    @Id
    private String userId;

    /**
     * 사용자에 대한 refreshToken 값입니다.
     * 사용자 로그인 시 발급되는 refreshToken이 저장됩니다.
     */
    private String refreshToken;
}
