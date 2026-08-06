package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.infra.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenRevoker {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    public void revoke(String refreshToken) {
        refreshTokenRepository.deleteByTokenHash(refreshTokenHasher.hash(refreshToken));
    }

    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}