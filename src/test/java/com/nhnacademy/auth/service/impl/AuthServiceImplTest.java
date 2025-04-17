package com.nhnacademy.auth.service.impl;

import com.nhnacademy.auth.adapter.UserAdapter;
import com.nhnacademy.auth.dto.UserSignInRequest;
import com.nhnacademy.auth.dto.UserSignUpRequest;
import com.nhnacademy.auth.provider.JwtProvider;
import com.nhnacademy.common.exception.NotFoundException;
import com.nhnacademy.common.exception.UnauthorizedException;
import com.nhnacademy.token.domain.RefreshToken;
import com.nhnacademy.token.dto.AccessTokenResponse;
import com.nhnacademy.token.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@Slf4j
@ExtendWith(SpringExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserAdapter userAdapter;

    @Mock
    JwtProvider jwtProvider;

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    RedisTemplate<String, String> redisTemplate;

    @Mock
    ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthServiceImpl authService;

    private UserSignUpRequest userSignUpRequest;
    private UserSignInRequest userSignInRequest;

    @BeforeEach
    void setUp() {

        userSignUpRequest = new UserSignUpRequest("auth", "auth@email.com", "api12345!");
        userSignInRequest = new UserSignInRequest("auth@email.com", "api12345!");


        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("회원가입: 201 성공")
    void signUp_201_success() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.CREATED);

        Mockito.when(userAdapter.createUser(Mockito.any(UserSignUpRequest.class))).thenReturn(responseEntity);
        Mockito.when(jwtProvider.createAccessToken(userSignUpRequest.getUserEmail())).thenReturn("mockToken");

        AccessTokenResponse accessTokenResponse = authService.signUp(userSignUpRequest);
        log.info("accessToken: "+accessTokenResponse.getAccessToken());

        assertNotNull(accessTokenResponse);
        assertEquals("mockToken", accessTokenResponse.getAccessToken());
        verify(userAdapter, times(1)).createUser(userSignUpRequest);
        verify(jwtProvider, times(1)).createAccessToken("auth@email.com");
    }

    @Test
    @DisplayName("AccessToken과 RefreshToken 생성 성공")
    void testCreateAccessAndRefreshToken_success() {
        String userId = "user123";
        String accessToken = "generatedAccessToken";
        String refreshToken = "generatedRefreshToken";

        when(jwtProvider.createAccessToken(userId)).thenReturn(accessToken);
        when(jwtProvider.createRefreshToken(userId)).thenReturn(refreshToken);
        when(jwtProvider.getRemainingExpiration(accessToken)).thenReturn(3600_000L); // 1시간

        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.empty());

        AccessTokenResponse response = authService.createAccessAndRefreshToken(userId);

        Assertions.assertEquals(accessToken, response.getAccessToken());
        Assertions.assertEquals(3600_000L, response.getTtl());

        verify(redisTemplate.opsForValue()).set("refreshToken:" + userId, refreshToken, 7, TimeUnit.DAYS);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("AccessToken 재발급 성공")
    void reissueAccessToken_success() {
        String userId = "test@nhnacademy.com";
        String accessToken = "sameValueToken";
        String refreshToken = "sameValueToken";

        when(jwtProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId))
                .thenReturn(Optional.of(new RefreshToken(userId, refreshToken)));

        String newAccessToken = "newAccessToken";

        when(jwtProvider.createAccessToken(userId)).thenReturn(newAccessToken);
        when(jwtProvider.getRemainingExpiration(accessToken)).thenReturn(60 * 1000L);
        when(jwtProvider.getRemainingExpiration(newAccessToken)).thenReturn(60 * 60 * 1000L);

        AccessTokenResponse response = authService.reissueAccessToken(accessToken);

        Assertions.assertEquals(newAccessToken, response.getAccessToken());
        Assertions.assertEquals(60 * 60 * 1000L, response.getTtl());

        verify(valueOperations).set("blacklist:" + accessToken, "logout", 60 * 1000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("AccessToken에서 userId 추출 실패 - 예외 발생")
    void testReissueAccessToken_nullUserId_throwsUnauthorized() {
        String accessToken = "invalidToken";

        when(jwtProvider.getUserIdFromToken(accessToken)).thenReturn(null); // 또는 throw new RuntimeException()

        assertThrows(UnauthorizedException.class, () -> authService.reissueAccessToken(accessToken));
    }

    @Test
    @DisplayName("RefreshToken이 저장되어 있지 않음 - 예외 발생")
    void testReissueAccessToken_refreshTokenNotFound_throwsUnauthorized() {
        String accessToken = "validToken";
        String userId = "user123";

        when(jwtProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.reissueAccessToken(accessToken));
    }

    @Test
    @DisplayName("저장된 RefreshToken과 AccessToken 불일치 - 예외 발생")
    void testReissueAccessToken_tokenMismatch_throwsUnauthorized() {
        String accessToken = "accessToken";
        String userId = "user123";
        String differentRefreshToken = "storedDifferentRefreshToken";

        when(jwtProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId))
                .thenReturn(Optional.of(new RefreshToken(userId, differentRefreshToken)));

        assertThrows(UnauthorizedException.class, () -> authService.reissueAccessToken(accessToken));
    }

    @Test
    @DisplayName("회원가입: 400 실패")
    void signUp_400_fail() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(userAdapter.createUser(userSignUpRequest)).thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.signUp(userSignUpRequest));
        assertEquals("회원가입에 실패하였습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그인: 200 성공")
    void signIn_200_success() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.OK);
        when(userAdapter.loginUser(userSignInRequest)).thenReturn(responseEntity);
        when(jwtProvider.createAccessToken(userSignInRequest.getUserEmail())).thenReturn("mockToken");

        AccessTokenResponse accessTokenResponse = authService.signIn(userSignInRequest);

        assertNotNull(accessTokenResponse);
        assertEquals("mockToken", accessTokenResponse.getAccessToken());
        verify(userAdapter, times(1)).loginUser(userSignInRequest);
        verify(jwtProvider, times(1)).createAccessToken(userSignInRequest.getUserEmail());
    }

    @Test
    @DisplayName("로그인: 400 실패")
    void signIn_400_fail() {

        ResponseEntity<Void> responseEntity = new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        when(userAdapter.loginUser(userSignInRequest)).thenReturn(responseEntity);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.signIn(userSignInRequest));
        assertEquals("로그인에 실패하였습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("로그아웃 시 Access와 RefreshToken 삭제")
    void testDeleteAccessAndRefreshToken_success() {
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";
        String userId = "user123";

        when(jwtProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId))
                .thenReturn(Optional.of(new RefreshToken(userId, refreshToken)));

        when(jwtProvider.getRemainingExpiration(accessToken)).thenReturn( 60 * 1000L);
        when(jwtProvider.getRemainingExpiration(refreshToken)).thenReturn( 60 * 60 * 1000L);

        authService.signOut(accessToken);

        verify(refreshTokenRepository).delete(any());
        verify(valueOperations).set("blacklist:" + accessToken, "logout", 60 * 1000L, TimeUnit.MILLISECONDS);
        verify(valueOperations).set("blacklist:" + refreshToken, "logout", 60 * 60 * 1000L, TimeUnit.MILLISECONDS);
    }

    @Test
    @DisplayName("로그아웃 시 NotFoundException 호출")
    void signOut(){
        String accessToken = "accessToken";
        String userId = "user123";

        when(jwtProvider.getUserIdFromToken(accessToken)).thenReturn(userId);
        when(refreshTokenRepository.findById(userId)).thenReturn(Optional.empty());

        Assertions.assertThrows(NotFoundException.class, () -> authService.signOut(accessToken));
    }
}