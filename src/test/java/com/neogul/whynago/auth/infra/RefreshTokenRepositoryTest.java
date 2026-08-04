package com.neogul.whynago.auth.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.auth.domain.RefreshToken;
import com.neogul.whynago.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RefreshTokenRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @DisplayName("저장된 토큰을 해시로 삭제하면 삭제된 행 수로 1을 반환한다.")
    @Test
    void deleteByTokenHash() {
        // given
        em.persistAndFlush(RefreshToken.create(1L, "hash-1"));
        em.clear();

        // when
        int deleted = refreshTokenRepository.deleteByTokenHash("hash-1");

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }

    @DisplayName("이미 폐기된 토큰을 삭제하면 삭제된 행 수로 0을 반환한다.")
    @Test
    void deleteByTokenHash_alreadyRevoked() {
        // when
        int deleted = refreshTokenRepository.deleteByTokenHash("unknown-hash");

        // then
        assertThat(deleted).isZero();
    }

    @DisplayName("사용자의 토큰을 삭제해도 다른 사용자의 토큰은 남는다.")
    @Test
    void deleteByUserId() {
        // given
        em.persistAndFlush(RefreshToken.create(1L, "hash-1"));
        em.persistAndFlush(RefreshToken.create(2L, "hash-2"));
        em.clear();

        // when
        int deleted = refreshTokenRepository.deleteByUserId(1L);

        // then
        assertThat(deleted).isEqualTo(1);
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getUserId)
                .containsExactly(2L);
    }
}