package com.nhnacademy.common.config;

import com.common.AESUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EncryptConfigTest {
    @Autowired
    AESUtil aesUtil;

    @Test
    void testAesUtilCreatedWithSecretKey() {
        assertNotNull(aesUtil);
    }
}