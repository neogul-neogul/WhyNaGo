package com.neogul.whynago.user.implement;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

class PasswordHasherTest {

    private final PasswordHasher passwordHasher = new PasswordHasher(new BCryptPasswordEncoder());

    @DisplayName("비밀번호를 해싱하면 원문과 다른 값이 반환된다.")
    @Test
    void hash() {
        // given
        String rawPassword = "password123";

        // when
        String hashed = passwordHasher.hash(rawPassword);

        // then
        assertThat(hashed).isNotEqualTo(rawPassword);
    }

    @DisplayName("원문과 해시가 일치하면 true를 반환한다.")
    @Test
    void matches() {
        // given
        String rawPassword = "password123";
        String hashed = passwordHasher.hash(rawPassword);

        // when & then
        assertThat(passwordHasher.matches(rawPassword, hashed)).isTrue();
    }

    @DisplayName("원문과 해시가 일치하지 않으면 false를 반환한다.")
    @Test
    void matches_wrongPassword() {
        // given
        String hashed = passwordHasher.hash("password123");

        // when & then
        assertThat(passwordHasher.matches("wrongPassword", hashed)).isFalse();
    }
}