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

    @DisplayName("토큰을 해시로 삭제하면 해당 토큰만 삭제된다.")
    @Test
    void deleteByTokenHash() {
        // given
        em.persistAndFlush(RefreshToken.create(1L, "hash-1"));
        em.persistAndFlush(RefreshToken.create(1L, "hash-2"));
        em.clear();

        // when
        refreshTokenRepository.deleteByTokenHash("hash-1");

        // then
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getTokenHash)
                .containsExactly("hash-2");
    }

    @DisplayName("이미 폐기된 토큰을 삭제해도 예외가 발생하지 않는다.")
    @Test
    void deleteByTokenHash_alreadyRevoked() {
        // when
        refreshTokenRepository.deleteByTokenHash("unknown-hash");

        // then
        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }

    @DisplayName("사용자의 토큰을 삭제해도 다른 사용자의 토큰은 남는다.")
    @Test
    void deleteByUserId() {
        // given
        em.persistAndFlush(RefreshToken.create(1L, "hash-1"));
        em.persistAndFlush(RefreshToken.create(2L, "hash-2"));
        em.clear();

        // when
        refreshTokenRepository.deleteByUserId(1L);

        // then
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getUserId)
                .containsExactly(2L);
    }
}