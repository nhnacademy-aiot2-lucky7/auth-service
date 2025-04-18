package com.nhnacademy.common.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DotenvConfigTest {

    @Autowired
    Dotenv dotenv;

    @Test
    @DisplayName(".env 로드 테스트")
    void dotenvBeanShouldLoad() {
        assertNotNull(dotenv);

        String value = dotenv.get("REDIS_HOST");
        assertEquals("s4.java21.net", value);
    }
}
