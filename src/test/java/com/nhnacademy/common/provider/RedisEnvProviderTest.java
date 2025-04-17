package com.nhnacademy.common.provider;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class RedisEnvProviderTest {

    @Test
    @DisplayName("정상적으로 환경변수를 읽는지 테스트")
    void validDotenvValues() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/test/resources") // .env가 루트에 있을 때
                .filename(".env")
                .load();

        RedisEnvProvider provider = new RedisEnvProvider(dotenv);

        assertThat(provider.getRedisHost()).isEqualTo("localhost");
        assertThat(provider.getRedisPassword()).isEqualTo("password123!@#");
        assertThat(provider.getRedisPort()).isEqualTo(6379);
    }

    @Test
    @DisplayName("REDIS_PORT 누락 시 예외 발생")
    void missingRedisPortThrowsException() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/test/resources/missing-port-env") // 여기에 REDIS_PORT 없는 .env 만들어주세요
                .filename(".env")
                .load();

        RedisEnvProvider provider = new RedisEnvProvider(dotenv);

        assertThatThrownBy(provider::getRedisPort)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("REDIS_PORT is not set");
    }

    @Test
    @DisplayName("REDIS_PORT가 숫자가 아닐 경우 예외 발생")
    void invalidRedisPortThrowsException() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/test/resources/invalid-port-env") // 여기에 REDIS_PORT=abc 같은 .env 만들어주세요
                .filename(".env")
                .load();

        RedisEnvProvider provider = new RedisEnvProvider(dotenv);

        assertThatThrownBy(provider::getRedisPort)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid REDIS_PORT value");
    }
}
