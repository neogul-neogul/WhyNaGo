package com.neogul.whynago.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.auth.domain.RefreshToken;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.fixture.SignUpCommandFixture;
import com.neogul.whynago.auth.implement.RefreshTokenHasher;
import com.neogul.whynago.auth.infra.GoogleIdTokenClient;
import com.neogul.whynago.auth.infra.RefreshTokenRepository;
import com.neogul.whynago.auth.infra.dto.GoogleUserInfo;
import com.neogul.whynago.auth.service.dto.GoogleLoginCommand;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.LogoutCommand;
import com.neogul.whynago.auth.service.dto.ReissueCommand;
import com.neogul.whynago.auth.service.dto.ReissueResult;
import com.neogul.whynago.auth.service.dto.SignUpCommand;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.implement.PasswordHasher;
import com.neogul.whynago.user.infra.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class AuthServiceTest extends IntegrationTestSupport {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordHasher passwordHasher;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private RefreshTokenHasher refreshTokenHasher;

    @MockitoBean
    private GoogleIdTokenClient googleIdTokenClient;

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

    @DisplayName("구글 계정으로 처음 로그인하면 사용자가 생성되고 토큰을 받는다.")
    @Test
    void googleLogin_newUser() {
        // given
        givenGoogleAccount("google-sub-1", "member@example.com", true);

        // when
        LoginResult result = authService.googleLogin(new GoogleLoginCommand("credential"));

        // then
        User saved = userRepository.findByProviderAndProviderId(AuthProvider.GOOGLE, "google-sub-1")
                .orElseThrow();
        assertThat(saved.getEmail().getValue()).isEqualTo("member@example.com");
        assertThat(saved.getPassword()).isNull();
        assertThat(saved.getNickname()).startsWith("u").hasSize(7);
        assertThat(saved.getPosition()).isEqualTo(Position.BACKEND);
        assertThat(result.tokenPair().accessToken()).isNotBlank();
        assertThat(result.tokenPair().refreshToken()).isNotBlank();
    }

    @DisplayName("이미 연동된 구글 계정으로 다시 로그인하면 사용자를 새로 만들지 않는다.")
    @Test
    void googleLogin_existingUser() {
        // given
        givenGoogleAccount("google-sub-1", "member@example.com", true);
        LoginResult first = authService.googleLogin(new GoogleLoginCommand("credential"));

        // when
        LoginResult second = authService.googleLogin(new GoogleLoginCommand("credential"));

        // then
        assertThat(second.userId()).isEqualTo(first.userId());
        assertThat(userRepository.findAll()).hasSize(1);
    }

    @DisplayName("일반 계정으로 가입된 이메일이면 구글 로그인에 실패한다.")
    @Test
    void googleLogin_localAccount() {
        // given
        authService.signup(SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .nickname("tester")
                .build());
        givenGoogleAccount("google-sub-1", "member@example.com", true);

        // when & then
        assertThatThrownBy(() -> authService.googleLogin(new GoogleLoginCommand("credential")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_LOCAL_ACCOUNT));
    }

    @DisplayName("구글 계정으로 가입된 이메일이면 일반 회원가입에 실패한다.")
    @Test
    void signup_socialAccount() {
        // given
        givenGoogleAccount("google-sub-1", "member@example.com", true);
        authService.googleLogin(new GoogleLoginCommand("credential"));
        SignUpCommand command = SignUpCommandFixture.signUpCommand()
                .email("member@example.com")
                .nickname("tester")
                .build();

        // when & then
        assertThatThrownBy(() -> authService.signup(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(UserErrorCode.USER_DUPLICATE_EMAIL_SOCIAL));
    }

    @DisplayName("검증에 실패한 id_token이면 구글 로그인에 실패한다.")
    @Test
    void googleLogin_invalidToken() {
        // given
        given(googleIdTokenClient.verify(any())).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> authService.googleLogin(new GoogleLoginCommand("invalid.credential")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_OAUTH_TOKEN_INVALID));
    }

    @DisplayName("구글 이메일이 검증되지 않았으면 구글 로그인에 실패한다.")
    @Test
    void googleLogin_emailNotVerified() {
        // given
        givenGoogleAccount("google-sub-1", "member@example.com", false);

        // when & then
        assertThatThrownBy(() -> authService.googleLogin(new GoogleLoginCommand("credential")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_OAUTH_TOKEN_INVALID));
    }

    @DisplayName("구글로 가입한 계정은 이메일과 비밀번호로 로그인할 수 없다.")
    @Test
    void login_socialAccount() {
        // given
        givenGoogleAccount("google-sub-1", "member@example.com", true);
        authService.googleLogin(new GoogleLoginCommand("credential"));
        LoginCommand command = new LoginCommand("member@example.com", "password123");

        // when & then
        assertThatThrownBy(() -> authService.login(command))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_SOCIAL_ACCOUNT));
    }

    @DisplayName("로그인하면 발급된 리프레시 토큰이 해시로 저장된다.")
    @Test
    void login_savesRefreshToken() {
        // when
        LoginResult result = signUpAndLogin();

        // then
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getTokenHash)
                .containsExactly(refreshTokenHasher.hash(result.tokenPair().refreshToken()));
    }

    @DisplayName("다시 로그인하면 이전 세션이 사라지고 활성 세션이 하나만 남는다.")
    @Test
    void login_replacesPreviousSession() {
        // given
        signUpAndLogin();

        // when
        LoginResult second = authService.login(new LoginCommand("member@example.com", "password123"));

        // then
        assertThat(refreshTokenRepository.findAll())
                .extracting(RefreshToken::getTokenHash)
                .containsExactly(refreshTokenHasher.hash(second.tokenPair().refreshToken()));
    }

    @DisplayName("리프레시 토큰으로 재발급하면 이전과 다른 토큰 쌍을 받고 새 토큰만 유효하게 남는다.")
    @Test
    void reissue() {
        // given
        LoginResult loggedIn = signUpAndLogin();
        String previousRefreshToken = loggedIn.tokenPair().refreshToken();

        // when
        ReissueResult result = authService.reissue(new ReissueCommand(previousRefreshToken));

        // then
        assertThat(result.accessToken()).isNotBlank().isNotEqualTo(loggedIn.tokenPair().accessToken());
        assertThat(result.refreshToken()).isNotBlank().isNotEqualTo(previousRefreshToken);
        assertThat(refreshTokenRepository.findAll())
                .filteredOn(token -> token.getUsedAt() == null)
                .extracting(RefreshToken::getTokenHash)
                .containsExactly(refreshTokenHasher.hash(result.refreshToken()));
    }

    @DisplayName("재발급에 사용한 리프레시 토큰은 사용 시각이 기록된다.")
    @Test
    void reissue_marksPreviousTokenUsed() {
        // given
        LoginResult loggedIn = signUpAndLogin();
        String previousRefreshToken = loggedIn.tokenPair().refreshToken();

        // when
        authService.reissue(new ReissueCommand(previousRefreshToken));

        // then
        assertThat(refreshTokenRepository.findByTokenHash(refreshTokenHasher.hash(previousRefreshToken)))
                .isPresent()
                .get()
                .extracting(RefreshToken::getUsedAt)
                .isNotNull();
    }

    @DisplayName("유예 시간 안에 같은 리프레시 토큰으로 다시 재발급하면 새 토큰 쌍을 받는다.")
    @Test
    void reissue_reusedTokenWithinGracePeriod() {
        // given
        LoginResult loggedIn = signUpAndLogin();
        String previousRefreshToken = loggedIn.tokenPair().refreshToken();
        ReissueResult first = authService.reissue(new ReissueCommand(previousRefreshToken));

        // when
        ReissueResult second = authService.reissue(new ReissueCommand(previousRefreshToken));

        // then
        assertThat(second.refreshToken()).isNotBlank().isNotEqualTo(first.refreshToken());
        assertThat(second.accessToken()).isNotBlank();
    }

    @DisplayName("다시 로그인해 폐기된 리프레시 토큰으로는 재발급에 실패한다.")
    @Test
    void reissue_tokenOfReplacedSession() {
        // given
        LoginResult first = signUpAndLogin();
        authService.login(new LoginCommand("member@example.com", "password123"));

        // when & then
        assertThatThrownBy(() -> authService.reissue(new ReissueCommand(first.tokenPair().refreshToken())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("서명이 올바르지 않은 리프레시 토큰으로는 재발급에 실패한다.")
    @Test
    void reissue_invalidToken() {
        // when & then
        assertThatThrownBy(() -> authService.reissue(new ReissueCommand("invalid.token.value")))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("저장된 적 없는 액세스 토큰으로는 재발급에 실패한다.")
    @Test
    void reissue_accessToken() {
        // given
        LoginResult loggedIn = signUpAndLogin();

        // when & then
        assertThatThrownBy(() -> authService.reissue(new ReissueCommand(loggedIn.tokenPair().accessToken())))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("로그아웃하면 저장된 리프레시 토큰이 폐기된다.")
    @Test
    void logout() {
        // given
        LoginResult loggedIn = signUpAndLogin();

        // when
        authService.logout(new LogoutCommand(loggedIn.tokenPair().refreshToken()));

        // then
        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }

    @DisplayName("로그아웃한 리프레시 토큰으로는 재발급에 실패한다.")
    @Test
    void reissue_loggedOutToken() {
        // given
        LoginResult loggedIn = signUpAndLogin();
        String refreshToken = loggedIn.tokenPair().refreshToken();
        authService.logout(new LogoutCommand(refreshToken));

        // when & then
        assertThatThrownBy(() -> authService.reissue(new ReissueCommand(refreshToken)))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("이미 폐기된 리프레시 토큰으로 로그아웃해도 예외가 발생하지 않는다.")
    @Test
    void logout_alreadyRevoked() {
        // given
        LoginResult loggedIn = signUpAndLogin();
        String refreshToken = loggedIn.tokenPair().refreshToken();
        authService.logout(new LogoutCommand(refreshToken));

        // when
        authService.logout(new LogoutCommand(refreshToken));

        // then
        assertThat(refreshTokenRepository.findAll()).isEmpty();
    }

    private void givenGoogleAccount(String sub, String email, boolean emailVerified) {
        given(googleIdTokenClient.verify(any()))
                .willReturn(Optional.of(new GoogleUserInfo(sub, email, emailVerified)));
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