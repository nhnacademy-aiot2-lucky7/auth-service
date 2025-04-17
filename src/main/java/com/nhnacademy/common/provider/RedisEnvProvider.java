package com.nhnacademy.common.provider;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisEnvProvider {

    private final Dotenv dotenv;

    public String getRedisHost() {
        return dotenv.get("REDIS_HOST");
    }

    public String getRedisPassword() {
        return dotenv.get("REDIS_PASSWORD");
    }

    public int getRedisPort() {
        String redisPortString = dotenv.get("REDIS_PORT");

        if (redisPortString == null) {
            throw new IllegalArgumentException("REDIS_PORT is not set in the environment variables.");
        }

        try {
            return Integer.parseInt(redisPortString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid REDIS_PORT value: " + redisPortString, e);
        }
    }
}
