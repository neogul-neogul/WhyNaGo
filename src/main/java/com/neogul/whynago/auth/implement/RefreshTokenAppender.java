package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.domain.RefreshToken;
import com.neogul.whynago.auth.infra.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RefreshTokenAppender {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;

    public void append(Long userId, String refreshToken) {
        refreshTokenRepository.save(
                RefreshToken.create(userId, refreshTokenHasher.hash(refreshToken)));
    }
}