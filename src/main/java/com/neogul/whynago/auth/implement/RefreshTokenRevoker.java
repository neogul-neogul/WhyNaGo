package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.infra.RefreshTokenRepository;
import com.neogul.whynago.common.exception.BusinessException;
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

    public void revokeForRotation(String refreshToken) {
        int revoked = refreshTokenRepository.deleteByTokenHash(refreshTokenHasher.hash(refreshToken));
        if (revoked == 0) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}