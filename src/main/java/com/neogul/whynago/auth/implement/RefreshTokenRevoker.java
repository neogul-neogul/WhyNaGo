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

    /** 로그아웃용. 이미 폐기된 토큰이어도 예외 없이 종료한다(멱등). */
    public void revoke(String refreshToken) {
        refreshTokenRepository.deleteByTokenHash(refreshTokenHasher.hash(refreshToken));
    }

    /**
     * 재발급용. 삭제된 행이 없으면 이미 폐기된 토큰이므로 거절한다.
     *
     * <p>조회 후 삭제가 아니라 삭제 건수로 존재를 판정한다. 조회와 삭제 사이에 틈이 없어야
     * 같은 토큰으로 동시 요청이 와도 하나만 통과한다.
     */
    public void revokeForRotation(String refreshToken) {
        int revoked = refreshTokenRepository.deleteByTokenHash(refreshTokenHasher.hash(refreshToken));
        if (revoked == 0) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    /** 로그인용. 한 계정에 활성 세션을 하나만 두기 위해 기존 세션을 모두 끊는다. */
    public void revokeAllByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}