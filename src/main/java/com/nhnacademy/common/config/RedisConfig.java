package com.nhnacademy.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스입니다.
 * <p>
 * 이 클래스는 RefreshToken, AccessToken 블랙리스트 용 Redis 인스턴스를 각각 설정하며,
 * 서로 다른 DB index를 사용하여 독립적으로 분리된 Redis 공간을 제공합니다.
 * </p>
 */
@Slf4j
@Configuration
@EnableRedisRepositories
@RequiredArgsConstructor
public class RedisConfig {

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password}")
    private String password;

    /**
     * Redis 연결 설정 생성 유틸리티
     *
     * @param database Redis DB 인덱스 (예: 270 = refresh, 271 = blacklist)
     * @return 구성된 RedisStandaloneConfiguration
     */
    private RedisStandaloneConfiguration redisConfig(int database) {
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(host);
        config.setPort(port);
        config.setPassword(RedisPassword.of(password));
        config.setDatabase(database);
        return config;
    }

    /**
     * RedisTemplate 생성 유틸리티
     *
     * @param factory LettuceConnectionFactory
     * @return 설정된 RedisTemplate
     */
    private RedisTemplate<String, Object> createTemplate(LettuceConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * RefreshToken 용 Redis ConnectionFactory
     *
     * @return LettuceConnectionFactory (DB 270)
     */
    @Primary
    @Bean(name = "refreshTokenRedisConnectionFactory")
    public LettuceConnectionFactory refreshTokenRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisConfig(270));
    }

    /**
     * AccessToken 블랙리스트 용 Redis ConnectionFactory
     *
     * @return LettuceConnectionFactory (DB 271)
     */
    @Bean(name = "accessTokenBlacklistRedisConnectionFactory")
    public LettuceConnectionFactory accessTokenBlacklistRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisConfig(271));
    }

    /**
     * RefreshToken RedisTemplate Bean
     *
     * @param connectionFactory refresh token 용 Redis 연결 팩토리
     * @return RedisTemplate
     */
    @Primary
    @Bean(name = "refreshTokenRedisTemplate")
    public RedisTemplate<String, Object> refreshTokenRedisTemplate(
            @Qualifier("refreshTokenRedisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return createTemplate(connectionFactory);
    }

    /**
     * AccessToken 블랙리스트 RedisTemplate Bean
     *
     * @param connectionFactory 블랙리스트용 Redis 연결 팩토리
     * @return RedisTemplate
     */
    @Bean(name = "accessTokenBlacklistRedisTemplate")
    public RedisTemplate<String, Object> accessTokenBlacklistRedisTemplate(
            @Qualifier("accessTokenBlacklistRedisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return createTemplate(connectionFactory);
    }
}
