package com.nhnacademy.common.config;

import com.nhnacademy.redis.provider.RedisProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisConfigTest {

    @Mock
    private RedisProvider redisProvider;

    @InjectMocks
    private RedisConfig redisConfig;

    @BeforeEach
    void setUp() {
        when(redisProvider.getRedisHost()).thenReturn("localhost");
        when(redisProvider.getRedisPort()).thenReturn(6379);
        when(redisProvider.getRedisPassword()).thenReturn("password123!@#");
    }

    @Test
    void testRedisConnectionFactory() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory();

        assertThat(factory).isNotNull();
        assertThat(factory.getStandaloneConfiguration()).satisfies(config -> {
            assertThat(config.getHostName()).isEqualTo("localhost");
            assertThat(config.getPort()).isEqualTo(6379);
            assertThat(config.getPassword()).isEqualTo(RedisPassword.of("password123!@#"));
            assertThat(config.getDatabase()).isEqualTo(270);
        });
    }

    @Test
    void testRedisTemplate() {
        LettuceConnectionFactory factory = redisConfig.redisConnectionFactory();
        RedisTemplate<String, Object> template = redisConfig.redisTemplate(factory);

        assertThat(template).isNotNull();
        assertThat(template.getConnectionFactory()).isEqualTo(factory);
        assertThat(template.getKeySerializer()).isInstanceOf(StringRedisSerializer.class);
        assertThat(template.getValueSerializer()).isInstanceOf(StringRedisSerializer.class);
    }
}
