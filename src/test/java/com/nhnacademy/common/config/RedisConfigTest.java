package com.nhnacademy.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
class RedisConfigTest {
    @Autowired
    private RedisConfig redisConfig;

    @Test
    void testRefreshTokenRedisConnectionFactory() {
        LettuceConnectionFactory factory = redisConfig.refreshTokenRedisConnectionFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration()).satisfies(config -> {
            assertThat(config.getHostName()).isEqualTo("s4.java21.net");
            assertThat(config.getPort()).isEqualTo(6379);
            assertThat(config.getPassword()).isEqualTo(RedisPassword.of("*N2vya7H@muDTwdNMR!"));
            assertThat(config.getDatabase()).isEqualTo(270);
        });
    }

    @Test
    void testAccessTokenBlacklistRedisConnectionFactory() {
        LettuceConnectionFactory factory = redisConfig.accessTokenBlacklistRedisConnectionFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration()).satisfies(config -> {
            assertThat(config.getHostName()).isEqualTo("s4.java21.net");
            assertThat(config.getPort()).isEqualTo(6379);
            assertThat(config.getPassword()).isEqualTo(RedisPassword.of("*N2vya7H@muDTwdNMR!"));
            assertThat(config.getDatabase()).isEqualTo(271);
        });
    }

    @Test
    void testRefreshTokenRedisTemplate() {
        LettuceConnectionFactory factory = redisConfig.refreshTokenRedisConnectionFactory();
        RedisTemplate<String, Object> template = redisConfig.refreshTokenRedisTemplate(factory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(factory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
    }

    @Test
    void testAccessTokenBlacklistRedisTemplate() {
        LettuceConnectionFactory factory = redisConfig.accessTokenBlacklistRedisConnectionFactory();
        RedisTemplate<String, Object> template = redisConfig.accessTokenBlacklistRedisTemplate(factory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(factory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
    }
}
