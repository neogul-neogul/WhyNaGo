package com.neogul.whynago.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.fixture.UserFixture;
import com.neogul.whynago.user.infra.UserRepository;
import com.neogul.whynago.user.service.dto.UserProfileResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserServiceTest extends IntegrationTestSupport {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("사용자의 최소 학습 목표를 조회한다.")
    @Test
    void getProfile() {
        User user = userRepository.save(UserFixture.user().build());
        user.updateDailyGoal(15);

        UserProfileResult result = userService.getProfile(user.getId());

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

    @DisplayName("사용자의 최소 학습 목표를 수정한다.")
    @Test
    void updateDailyGoal() {
        User user = userRepository.save(UserFixture.user().build());

        UserProfileResult result = userService.updateDailyGoal(user.getId(), 20);

        assertThat(result.dailyGoal()).isEqualTo(20);
        assertThat(userRepository.findById(user.getId()).orElseThrow().getDailyGoal()).isEqualTo(20);
    }

    @DisplayName("존재하지 않는 사용자의 목표를 수정하면 예외가 발생한다.")
    @Test
    void updateDailyGoal_notFound() {
        assertThatThrownBy(() -> userService.updateDailyGoal(999L, 20))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_NOT_FOUND));
    }
}