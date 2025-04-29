package com.nhnacademy.auth.service.refresh_token;

public interface RefreshTokenService {
    void setRefreshToken(String refreshToken, String userId);
    void removeRefreshToken(String accessToken);
    String reissueAccessToken(String accessToken);
}
