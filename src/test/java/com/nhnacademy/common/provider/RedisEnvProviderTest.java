package com.nhnacademy.common.provider;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest
class RedisEnvProviderTest {

    @Autowired
    private Environment env;

    @Test
    @DisplayName("Dotenv + Environment 환경변수 읽기 테스트")
    void testReadEnvValues() {
        // 테스트용 dotenv 로딩 (.env는 src/test/resources에 있다고 가정)
        Dotenv dotenv = Dotenv.configure()
                .directory("src/test/resources")
                .filename(".env")
                .load();

        RedisProvider provider = new RedisProvider(dotenv, env);

        Assertions.assertEquals("localhost", provider.getRedisHost());
        Assertions.assertEquals(6379, provider.getRedisPort());
        Assertions.assertEquals("password123!@#", provider.getRedisPassword());
    }
}