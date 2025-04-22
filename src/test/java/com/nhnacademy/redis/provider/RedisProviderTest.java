package com.nhnacademy.redis.provider;

import com.nhnacademy.common.exception.InvalidRedisConfigException;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;


@SpringBootTest
class RedisProviderTest {

    @Autowired
    private Environment env;

    @Test
    @DisplayName("Dotenv + Environment 환경변수 읽기 테스트")
    void testReadEnvValues() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/test/resources")
                .filename(".env")
                .load();

        RedisProvider provider = new RedisProvider(dotenv, env);

        Assertions.assertEquals("localhost", provider.getRedisHost());
        Assertions.assertEquals("password123!@#", provider.getRedisPassword());
    }

    @Test
    @DisplayName("REDIS_PORT가 잘못된 경우 예외 처리 테스트")
    void testGetRedisPort_InvalidPort() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/test/resources")
                .filename(".env")
                .load();

        RedisProvider provider = new RedisProvider(dotenv, env);

        Assertions.assertThrows(InvalidRedisConfigException.class, provider::getRedisPort);
    }
}