package com.neogul.whynago.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class EmailTest {

    @DisplayName("올바른 형식의 이메일이면 생성된다.")
    @Test
    void create() {
        // when
        Email email = new Email("test@example.com");

        // then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }

    @DisplayName("형식이 올바르지 않은 이메일이면 예외가 발생한다.")
    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"invalid", "test@", "@example.com", "test example.com", "test@example"})
    void create_invalidFormat(String value) {
        // when & then
        assertThatThrownBy(() -> new Email(value))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_INVALID_EMAIL));
    }
}