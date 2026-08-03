package com.neogul.whynago.user.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.exception.UserErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class UserTest {

    @DisplayName("사용자를 생성하면 직무는 BACKEND로 고정된다.")
    @Test
    void create() {
        // when
        User user = User.create("test@example.com", "hashedPassword", "tester");

        // then
        assertThat(user.getEmail().getValue()).isEqualTo("test@example.com");
        assertThat(user.getNickname()).isEqualTo("tester");
        assertThat(user.getPosition()).isEqualTo(Position.BACKEND);
        assertThat(user.getDailyGoal()).isEqualTo(10);
    }

    @DisplayName("최소 학습 목표를 변경한다.")
    @Test
    void updateDailyGoal() {
        // given
        User user = User.create("test@example.com", "hashedPassword", "tester");

        // when
        user.updateDailyGoal(20);

        // then
        assertThat(user.getDailyGoal()).isEqualTo(20);
    }

    @DisplayName("프로필을 수정한다.")
    @Test
    void updateProfile() {
        // given
        User user = User.create("test@example.com", "hashedPassword", "tester");

        // when
        user.updateProfile("changed@example.com", "changed", Position.FRONTEND, 20);

        // then
        assertThat(user.getEmail().getValue()).isEqualTo("changed@example.com");
        assertThat(user.getNickname()).isEqualTo("changed");
        assertThat(user.getPosition()).isEqualTo(Position.FRONTEND);
        assertThat(user.getDailyGoal()).isEqualTo(20);
    }

    @DisplayName("프로필 수정 시 닉네임 형식이 올바르지 않으면 예외가 발생한다.")
    @Test
    void updateProfile_invalidNickname() {
        // given
        User user = User.create("test@example.com", "hashedPassword", "tester");

        // when & then
        assertThatThrownBy(() -> user.updateProfile("test@example.com", "ab", Position.BACKEND, 10))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_INVALID_NICKNAME));
    }

    @DisplayName("닉네임 형식이 올바르지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "abc", "abcdefghi"})
    void create_invalidNickname(String nickname) {
        // when & then
        assertThatThrownBy(() -> User.create("test@example.com", "hashedPassword", nickname))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_INVALID_NICKNAME));
    }
}