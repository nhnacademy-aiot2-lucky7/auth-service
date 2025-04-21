package com.nhnacademy.common.provider;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisProvider {

    private final Dotenv dotenv;
    private final Environment env;

    public String getRedisHost() {
        return getProperty("REDIS_HOST", "redis.host");
    }

    public String getRedisPassword() {
        return getProperty("REDIS_PASSWORD", "redis.password");
    }

    public int getRedisPort() {
        String value = getProperty("REDIS_PORT", "redis.port");
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid redis port: " + value, e);
        }
    }

    private String getProperty(String dotenvKey, String envKey) {
        String value = dotenv.get(dotenvKey);

        if (value == null) {
            value = env.getProperty(envKey);
        }

        if (value == null) {
            value = System.getenv(dotenvKey); // 마지막 fallback
        }

        if (value == null) {
            throw new IllegalArgumentException(dotenvKey + " or " + envKey + " is not set.");
        }

        return value;
    }
}
