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
}
