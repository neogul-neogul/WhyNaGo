package com.neogul.whynago.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 6, 12, 0);

    @DisplayName("토큰을 사용 처리하면 사용 시각이 기록된다.")
    @Test
    void markUsed() {
        // given
        RefreshToken token = RefreshToken.create(1L, "hash-1");

        // when
        token.markUsed(NOW);

        // then
        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @DisplayName("이미 사용 처리된 토큰을 다시 사용 처리해도 사용 시각은 갱신되지 않는다.")
    @Test
    void markUsed_alreadyUsed() {
        // given
        RefreshToken token = RefreshToken.create(1L, "hash-1");
        token.markUsed(NOW);

        // when
        token.markUsed(NOW.plusSeconds(5));

        // then
        assertThat(token.getUsedAt()).isEqualTo(NOW);
    }

    @DisplayName("유예 시작 시각보다 앞서 사용된 토큰이면 참을 반환한다.")
    @Test
    void isUsedBefore() {
        // given
        RefreshToken token = RefreshToken.create(1L, "hash-1");
        token.markUsed(NOW);

        // when
        boolean usedBefore = token.isUsedBefore(NOW.plusSeconds(10));

        // then
        assertThat(usedBefore).isTrue();
    }

    @DisplayName("유예 시작 시각과 같은 시각에 사용된 토큰이면 참을 반환한다.")
    @Test
    void isUsedBefore_sameAsGraceStart() {
        // given
        RefreshToken token = RefreshToken.create(1L, "hash-1");
        token.markUsed(NOW);

        // when
        boolean usedBefore = token.isUsedBefore(NOW);

        // then
        assertThat(usedBefore).isTrue();
    }

    @DisplayName("유예 시작 시각 이후에 사용된 토큰이면 거짓을 반환한다.")
    @Test
    void isUsedBefore_withinGracePeriod() {
        // given
        RefreshToken token = RefreshToken.create(1L, "hash-1");
        token.markUsed(NOW);

        // when
        boolean usedBefore = token.isUsedBefore(NOW.minusSeconds(10));

        // then
        assertThat(usedBefore).isFalse();
    }

    @DisplayName("사용된 적 없는 토큰이면 거짓을 반환한다.")
    @Test
    void isUsedBefore_unused() {
        // given
        RefreshToken token = RefreshToken.create(1L, "hash-1");

        // when
        boolean usedBefore = token.isUsedBefore(NOW);

        // then
        assertThat(usedBefore).isFalse();
    }
}