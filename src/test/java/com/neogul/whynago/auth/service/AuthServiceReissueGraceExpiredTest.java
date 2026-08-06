package com.neogul.whynago.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.fixture.SignUpCommandFixture;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.ReissueCommand;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "jwt.refresh.grace-period=0")
class AuthServiceReissueGraceExpiredTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @DisplayName("유예 시간이 지난 리프레시 토큰으로 재발급하면 실패한다.")
    @Test
    void reissue_reusedTokenAfterGracePeriod() {
        // given
        LoginResult loggedIn = signUpAndLogin();
        String previousRefreshToken = loggedIn.tokenPair().refreshToken();
        authService.reissue(new ReissueCommand(previousRefreshToken));

        // when & then
        assertThatThrownBy(() -> authService.reissue(new ReissueCommand(previousRefreshToken)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    private LoginResult signUpAndLogin() {
        authService.signup(SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .password("password123")
                .nickname("tester")
                .build());
        return authService.login(new LoginCommand("member@example.com", "password123"));
    }
}