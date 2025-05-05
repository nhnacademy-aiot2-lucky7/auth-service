package com.nhnacademy.service.blacklist.impl;

import com.nhnacademy.token.provider.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class BlacklistServiceImplTest {
    @Mock
    RedisTemplate<String, Object> template;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    ValueOperations<String, Object> valueOperations;

    @InjectMocks
    BlacklistServiceImpl blacklistService;

    @Test
    @DisplayName("add blacklist test")
    void addBlacklistTest() {
        String token = "access_token";
        long ttl = 3600000L;

        when(jwtProvider.getRemainingExpiration(anyString())).thenReturn(ttl);
        when(template.opsForValue()).thenReturn(valueOperations);

        blacklistService.addBlacklist(token);

        verify(valueOperations, times(1)).set(
                "blacklist:access_token",
                "logout",
                ttl,
                TimeUnit.MILLISECONDS
        );
    }
}