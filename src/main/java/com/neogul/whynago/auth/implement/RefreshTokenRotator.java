package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.domain.RefreshToken;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.infra.RefreshTokenRepository;
import com.neogul.whynago.common.exception.BusinessException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RefreshTokenRotator {

    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenHasher refreshTokenHasher;
    private final long gracePeriod;

    public RefreshTokenRotator(
            RefreshTokenRepository refreshTokenRepository,
            RefreshTokenHasher refreshTokenHasher,
            @Value("${jwt.refresh.grace-period}") long gracePeriod) {
        this.refreshTokenRepository = refreshTokenRepository;
        this.refreshTokenHasher = refreshTokenHasher;
        this.gracePeriod = gracePeriod;
    }

    // 여러 탭이 같은 refresh token으로 동시에 재발급하면 한쪽이 강제 로그아웃되므로,
    // 유예 시간 안의 재사용은 허용한다.
    public void rotate(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(refreshToken))
                .orElseThrow(() -> new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID));

        LocalDateTime now = LocalDateTime.now();
        if (token.isUsedBefore(now.minus(gracePeriod, ChronoUnit.MILLIS))) {
            log.warn("이미 사용된 refresh token 재사용 - userId={}", token.getUserId());
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
        token.markUsed(now);
    }
}