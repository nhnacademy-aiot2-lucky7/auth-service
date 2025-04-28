package com.nhnacademy.common.config;

import com.common.AESUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EncryptConfig {
    @Value("${aes.secret.key}")
    private String secretKey;

    @Bean
    public AESUtil aesUtil() {
        AESUtil aesUtil = new AESUtil();
        aesUtil.setKey(secretKey);
        return aesUtil;
    }
}
