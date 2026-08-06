package com.neogul.whynago.auth.implement;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefreshTokenHasherTest {

    private final RefreshTokenHasher refreshTokenHasher = new RefreshTokenHasher();

    @DisplayName("같은 토큰을 해시하면 항상 같은 값이 나온다.")
    @Test
    void hash() {
        // given
        String refreshToken = "refresh.token.value";

        // when
        String first = refreshTokenHasher.hash(refreshToken);
        String second = refreshTokenHasher.hash(refreshToken);

        // then
        assertThat(first).isEqualTo(second);
    }

    @DisplayName("다른 토큰을 해시하면 다른 값이 나온다.")
    @Test
    void hash_differentToken() {
        // when
        String first = refreshTokenHasher.hash("refresh.token.one");
        String second = refreshTokenHasher.hash("refresh.token.two");

        // then
        assertThat(first).isNotEqualTo(second);
    }

    @DisplayName("해시 결과는 원문을 담지 않는 64자 16진 문자열이다.")
    @Test
    void hash_doesNotExposeRawToken() {
        // given
        String refreshToken = "refresh.token.value";

        // when
        String hashed = refreshTokenHasher.hash(refreshToken);

        // then
        assertThat(hashed).hasSize(64).matches("[0-9a-f]+").doesNotContain(refreshToken);
    }
}