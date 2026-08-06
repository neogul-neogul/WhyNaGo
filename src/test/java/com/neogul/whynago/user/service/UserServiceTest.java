package com.neogul.whynago.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.fixture.UserFixture;
import com.neogul.whynago.user.infra.UserRepository;
import com.neogul.whynago.user.service.dto.UpdateProfileCommand;
import com.neogul.whynago.user.service.dto.UserProfileResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("사용자의 프로필을 조회한다.")
    @Test
    void getProfile() {
        User user = userRepository.save(UserFixture.user().email("member@example.com").nickname("tester").build());
        user.updateDailyGoal(15);

        UserProfileResult result = userService.getProfile(user.getId());

        assertThat(result.email()).isEqualTo("member@example.com");
        assertThat(result.nickname()).isEqualTo("tester");
        assertThat(result.position()).isEqualTo(Position.BACKEND);
        assertThat(result.dailyGoal()).isEqualTo(15);
    }

    @DisplayName("존재하지 않는 사용자를 조회하면 예외가 발생한다.")
    @Test
    void getProfile_notFound() {
        assertThatThrownBy(() -> userService.getProfile(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }

    @DisplayName("사용자의 프로필을 수정한다.")
    @Test
    void updateProfile() {
        User user = userRepository.save(UserFixture.user().email("member@example.com").nickname("tester").build());
        UpdateProfileCommand command = new UpdateProfileCommand("changed", Position.FRONTEND, 20);

        UserProfileResult result = userService.updateProfile(user.getId(), command);

        assertThat(result.email()).isEqualTo("member@example.com");
        assertThat(result.nickname()).isEqualTo("changed");
        assertThat(result.position()).isEqualTo(Position.FRONTEND);
        assertThat(result.dailyGoal()).isEqualTo(20);
    }

    @DisplayName("자신의 기존 닉네임으로는 그대로 수정할 수 있다.")
    @Test
    void updateProfile_sameNickname() {
        User user = userRepository.save(UserFixture.user().email("member@example.com").nickname("tester").build());
        UpdateProfileCommand command = new UpdateProfileCommand("tester", Position.BACKEND, 20);

        UserProfileResult result = userService.updateProfile(user.getId(), command);

        assertThat(result.dailyGoal()).isEqualTo(20);
    }

    @DisplayName("다른 사용자가 이미 사용 중인 닉네임으로 수정하면 예외가 발생한다.")
    @Test
    void updateProfile_duplicateNickname() {
        userRepository.save(UserFixture.user().email("taken@example.com").nickname("taken").build());
        User user = userRepository.save(UserFixture.user().email("member@example.com").nickname("tester").build());
        UpdateProfileCommand command = new UpdateProfileCommand("taken", Position.BACKEND, 10);

        assertThatThrownBy(() -> userService.updateProfile(user.getId(), command))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_DUPLICATE_NICKNAME));
    }

    @DisplayName("존재하지 않는 사용자의 프로필을 수정하면 예외가 발생한다.")
    @Test
    void updateProfile_notFound() {
        UpdateProfileCommand command = new UpdateProfileCommand("tester", Position.BACKEND, 20);

        assertThatThrownBy(() -> userService.updateProfile(999L, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }
}
