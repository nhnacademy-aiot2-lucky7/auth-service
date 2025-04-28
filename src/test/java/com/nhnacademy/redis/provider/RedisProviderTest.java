package com.nhnacademy.redis.provider;

import com.nhnacademy.common.exception.InvalidRedisConfigException;
import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@SpringBootTest
class RedisProviderTest {

    @InjectMocks
    RedisProvider provider;
    @Mock
    Dotenv dotenv;
    @Mock
    Environment env;

    @Test
    @DisplayName("dotenv에서 host 읽기")
    void getHostFromDotenv() {
        when(dotenv.get("REDIS_HOST")).thenReturn("redis host");

        assertNotNull(provider.getRedisHost());
    }

    @Test
    @DisplayName("Enviroment에서 host 읽기")
    void getHostFromEnv() {
        when(dotenv.get("REDIS_HOST")).thenReturn(null);
        when(env.getProperty("redis.host")).thenReturn("redis host");

        assertNotNull(provider.getRedisHost());
    }

    @Test
    @DisplayName("dotenv + Enviroment에서 host 읽기 실패")
    void failGetHost() {
        when(dotenv.get("REDIS_HOST")).thenReturn(null);
        when(env.getProperty("redis.host")).thenReturn(null);

        assertThrows(InvalidRedisConfigException.class, () -> provider.getRedisHost());
    }

    @Test
    @DisplayName("잘못된 포트 번호")
    void invalidPortNumberTest() {
        when(dotenv.get("REDIS_PORT")).thenReturn("djafkl");

        assertThrows(InvalidRedisConfigException.class, () -> provider.getRedisPort());
    }

    @Test
    @DisplayName("잘못된 포트 번호2")
    void invalidPortNumberTest2() {
        when(dotenv.get("REDIS_PORT")).thenReturn(null);
        when(env.getProperty("redis.port")).thenReturn("adfjakdl");

        assertThrows(InvalidRedisConfigException.class, () -> provider.getRedisPort());
    }
}