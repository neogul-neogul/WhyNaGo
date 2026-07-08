package com.neogul.whynago.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.fixture.SignUpCommandFixture;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.SignUpCommand;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.implement.PasswordHasher;
import com.neogul.whynago.user.infra.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AuthServiceTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @DisplayName("회원가입에 성공하면 사용자가 저장되고 비밀번호는 암호화되어 저장된다.")
    @Test
    void signup() {
        // given
        SignUpCommand command = SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .password("password123")
                .nickname("tester")
                .build();

        // when
        Long userId = authService.signup(command);

        // then
        User saved = userRepository.findById(userId).orElseThrow();
        assertThat(saved.getEmail().getValue()).isEqualTo("member@example.com");
        assertThat(saved.getNickname()).isEqualTo("tester");
        assertThat(saved.getPassword()).isNotEqualTo("password123");
        assertThat(passwordHasher.matches("password123", saved.getPassword())).isTrue();
    }

    @DisplayName("이미 사용 중인 이메일이면 회원가입에 실패한다.")
    @Test
    void signup_duplicateEmail() {
        // given
        authService.signup(SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .nickname("tester")
                .build());
        SignUpCommand duplicate = SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .nickname("other")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.signup(duplicate))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_DUPLICATE_EMAIL));
    }

    @DisplayName("이미 사용 중인 닉네임이면 회원가입에 실패한다.")
    @Test
    void signup_duplicateNickname() {
        // given
        authService.signup(SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .nickname("tester")
                .build());
        SignUpCommand duplicate = SignUpCommandFixture.signUpCommand()
                .email("other@example.com")
                .nickname("tester")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.signup(duplicate))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_DUPLICATE_NICKNAME));
    }

    @DisplayName("올바른 이메일과 비밀번호로 로그인하면 토큰과 사용자 정보를 반환한다.")
    @Test
    void login() {
        // given
        authService.signup(SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .password("password123")
                .nickname("tester")
                .build());
        LoginCommand command = new LoginCommand("member@example.com", "password123");

        // when
        LoginResult result = authService.login(command);

        // then
        assertThat(result.tokenPair().accessToken()).isNotBlank();
        assertThat(result.tokenPair().refreshToken()).isNotBlank();
        assertThat(result.email()).isEqualTo("member@example.com");
        assertThat(result.nickname()).isEqualTo("tester");
        assertThat(result.position()).isEqualTo(Position.BACKEND);
    }

    @DisplayName("등록되지 않은 이메일이면 로그인에 실패한다.")
    @Test
    void login_notFoundEmail() {
        // given
        LoginCommand command = new LoginCommand("unknown@example.com", "password123");

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_LOGIN_FAILED));
    }

    @DisplayName("비밀번호가 일치하지 않으면 로그인에 실패한다.")
    @Test
    void login_wrongPassword() {
        // given
        authService.signup(SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .password("password123")
                .nickname("tester")
                .build());
        LoginCommand command = new LoginCommand("member@example.com", "wrongpassword");

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_LOGIN_FAILED));
    }
}