package com.nhnacademy.redis.provider;

import com.nhnacademy.common.exception.InvalidRedisConfigException;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * {@code RedisProvider} 클래스는 Redis 접속을 위한 설정 값을 환경 변수 또는 환경 설정 파일에서 로드하는 기능을 제공합니다.
 *
 * <p>우선순위는 다음과 같습니다:</p>
 * <ol>
 *     <li>{@link Dotenv} 로드된 .env 파일</li>
 *     <li>{@link Environment} (Spring 환경 설정)</li>
 *     <li>{@link System#getenv(String)} 시스템 환경 변수</li>
 * </ol>
 *
 * <p>호스트, 포트, 비밀번호를 안전하게 로드하며, 값이 없거나 포트 값이 숫자가 아닌 경우 예외를 발생시킵니다.</p>
 */
@Component
@RequiredArgsConstructor
public class RedisProvider {

    private final Dotenv dotenv;
    private final Environment env;

    /**
     * Redis 호스트를 반환합니다.
     *
     * @return Redis 서버의 호스트 이름
     * @throws InvalidRedisConfigException 호스트 설정값이 존재하지 않는 경우
     */
    public String getRedisHost() {
        return getProperty("REDIS_HOST", "redis.host");
    }

    /**
     * Redis 비밀번호를 반환합니다.
     *
     * @return Redis 서버 비밀번호
     * @throws InvalidRedisConfigException 비밀번호 설정값이 존재하지 않는 경우
     */
    public String getRedisPassword() {
        return getProperty("REDIS_PASSWORD", "redis.password");
    }

    /**
     * Redis 포트를 반환합니다.
     *
     * @return Redis 서버의 포트 번호
     * @throws InvalidRedisConfigException 포트가 설정되지 않았거나 숫자가 아닌 경우
     */
    public Integer getRedisPort() {
        String value = getProperty("REDIS_PORT", "redis.port");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new InvalidRedisConfigException("잘못된 redis port: " + value);
        }
    }

    /**
     * 설정값을 가져오는 내부 메서드입니다.
     * Dotenv → Spring Environment → System.getenv 순으로 값을 찾습니다.
     *
     * @param dotenvKey .env 환경 변수 키
     * @param envKey    Spring 설정 키
     * @return 설정된 값
     * @throws InvalidRedisConfigException 값이 존재하지 않는 경우
     */
    private String getProperty(String dotenvKey, String envKey) {
        String value = dotenv.get(dotenvKey);

        if (value == null) {
            value = env.getProperty(envKey);
        }

        if (value == null) {
            value = System.getenv(dotenvKey); // 마지막 fallback
        }

        if (value == null) {
            throw new InvalidRedisConfigException(dotenvKey + " or " + envKey + "가 설정되지 않았습니다.");
        }

        return value;
    }
}
