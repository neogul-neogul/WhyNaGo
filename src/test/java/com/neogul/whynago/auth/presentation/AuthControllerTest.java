package com.neogul.whynago.auth.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.fixture.LoginRequestFixture;
import com.neogul.whynago.auth.fixture.SignUpRequestFixture;
import com.neogul.whynago.auth.presentation.dto.GoogleLoginRequest;
import com.neogul.whynago.auth.presentation.dto.LoginRequest;
import com.neogul.whynago.auth.presentation.dto.LogoutRequest;
import com.neogul.whynago.auth.presentation.dto.ReissueRequest;
import com.neogul.whynago.auth.presentation.dto.SignUpRequest;
import com.neogul.whynago.auth.service.dto.ReissueResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.exception.UserErrorCode;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;

class AuthControllerTest extends ControllerTestSupport {

    @DisplayName("회원가입에 성공하면 201 Created와 userId를 응답한다.")
    @Test
    void signup() {
        // given
        given(authService.signup(any())).willReturn(1L);
        SignUpRequest request = SignUpRequestFixture.signUpRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/signup")
                .then()
                .statusCode(HttpStatus.CREATED.value())
                .body("userId", equalTo(1));
    }

    @DisplayName("요청 형식이 올바르지 않으면 400 Bad Request를 응답한다.")
    @ParameterizedTest
    @MethodSource("invalidRequests")
    void signup_invalidRequest(SignUpRequest request) {
        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/signup")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<SignUpRequest> invalidRequests() {
        return Stream.of(
                SignUpRequestFixture.signUpRequest().email("invalid").build(),
                SignUpRequestFixture.signUpRequest().email("").build(),
                SignUpRequestFixture.signUpRequest().password("short12").build(),
                SignUpRequestFixture.signUpRequest().password("thispasswordistoolong").build(),
                SignUpRequestFixture.signUpRequest().nickname("abc").build(),
                SignUpRequestFixture.signUpRequest().nickname("abcdefghi").build()
        );
    }

    @DisplayName("이미 사용 중인 이메일이면 409 Conflict를 응답한다.")
    @Test
    void signup_duplicateEmail() {
        // given
        given(authService.signup(any()))
                .willThrow(new BusinessException(UserErrorCode.USER_DUPLICATE_EMAIL));
        SignUpRequest request = SignUpRequestFixture.signUpRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/signup")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("code", equalTo("USER_DUPLICATE_EMAIL"));
    }

    @DisplayName("이미 사용 중인 닉네임이면 409 Conflict를 응답한다.")
    @Test
    void signup_duplicateNickname() {
        // given
        given(authService.signup(any()))
                .willThrow(new BusinessException(UserErrorCode.USER_DUPLICATE_NICKNAME));
        SignUpRequest request = SignUpRequestFixture.signUpRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/signup")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("code", equalTo("USER_DUPLICATE_NICKNAME"));
    }

    @DisplayName("로그인에 성공하면 200 OK와 함께 토큰 및 사용자 정보를 응답한다.")
    @Test
    void login() {
        // given
        given(authService.login(any()))
                .willReturn(new LoginResult(
                        new TokenPair("access.token", "refresh.token"),
                        1L,
                        "test@example.com",
                        "테스터",
                        Position.BACKEND));
        LoginRequest request = LoginRequestFixture.loginRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", equalTo("access.token"))
                .body("refreshToken", equalTo("refresh.token"))
                .body("id", equalTo(1))
                .body("email", equalTo("test@example.com"))
                .body("nickname", equalTo("테스터"))
                .body("position", equalTo("BACKEND"));
    }

    @DisplayName("로그인 요청 형식이 올바르지 않으면 400 Bad Request를 응답한다.")
    @ParameterizedTest
    @MethodSource("invalidLoginRequests")
    void login_invalidRequest(LoginRequest request) {
        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    static Stream<LoginRequest> invalidLoginRequests() {
        return Stream.of(
                LoginRequestFixture.loginRequest().email("").build(),
                LoginRequestFixture.loginRequest().password("").build()
        );
    }

    @DisplayName("이메일 또는 비밀번호가 올바르지 않으면 401 Unauthorized를 응답한다.")
    @Test
    void login_loginFailed() {
        // given
        given(authService.login(any()))
                .willThrow(new BusinessException(AuthErrorCode.AUTH_LOGIN_FAILED));
        LoginRequest request = LoginRequestFixture.loginRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AUTH_LOGIN_FAILED"));
    }

    @DisplayName("구글 로그인에 성공하면 200 OK와 함께 토큰 및 사용자 정보를 응답한다.")
    @Test
    void googleLogin() {
        // given
        given(authService.googleLogin(any()))
                .willReturn(new LoginResult(
                        new TokenPair("access.token", "refresh.token"),
                        1L,
                        "test@example.com",
                        "u123456",
                        Position.BACKEND));

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new GoogleLoginRequest("credential"))
                .when()
                .post("/api/auth/login/google")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", equalTo("access.token"))
                .body("refreshToken", equalTo("refresh.token"))
                .body("id", equalTo(1))
                .body("email", equalTo("test@example.com"))
                .body("nickname", equalTo("u123456"))
                .body("position", equalTo("BACKEND"));
    }

    @DisplayName("구글 로그인 요청에 credential이 없으면 400 Bad Request를 응답한다.")
    @Test
    void googleLogin_blankCredential() {
        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new GoogleLoginRequest(""))
                .when()
                .post("/api/auth/login/google")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("검증에 실패한 id_token이면 401 Unauthorized를 응답한다.")
    @Test
    void googleLogin_invalidToken() {
        // given
        given(authService.googleLogin(any()))
                .willThrow(new BusinessException(AuthErrorCode.AUTH_OAUTH_TOKEN_INVALID));

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new GoogleLoginRequest("invalid.credential"))
                .when()
                .post("/api/auth/login/google")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AUTH_OAUTH_TOKEN_INVALID"));
    }

    @DisplayName("일반 계정으로 가입된 이메일로 구글 로그인하면 409 Conflict를 응답한다.")
    @Test
    void googleLogin_localAccount() {
        // given
        given(authService.googleLogin(any()))
                .willThrow(new BusinessException(AuthErrorCode.AUTH_LOCAL_ACCOUNT));

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new GoogleLoginRequest("credential"))
                .when()
                .post("/api/auth/login/google")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("code", equalTo("AUTH_LOCAL_ACCOUNT"));
    }

    @DisplayName("구글 계정으로 가입된 이메일로 회원가입하면 409 Conflict를 응답한다.")
    @Test
    void signup_socialAccount() {
        // given
        given(authService.signup(any()))
                .willThrow(new BusinessException(UserErrorCode.USER_DUPLICATE_EMAIL_SOCIAL));
        SignUpRequest request = SignUpRequestFixture.signUpRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/signup")
                .then()
                .statusCode(HttpStatus.CONFLICT.value())
                .body("code", equalTo("USER_DUPLICATE_EMAIL_SOCIAL"));
    }

    @DisplayName("구글로 가입한 계정으로 일반 로그인하면 401 Unauthorized를 응답한다.")
    @Test
    void login_socialAccount() {
        // given
        given(authService.login(any()))
                .willThrow(new BusinessException(AuthErrorCode.AUTH_SOCIAL_ACCOUNT));
        LoginRequest request = LoginRequestFixture.loginRequest().build();

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AUTH_SOCIAL_ACCOUNT"));
    }

    @DisplayName("재발급에 성공하면 200 OK와 새 토큰 쌍을 응답한다.")
    @Test
    void reissue() {
        // given
        given(authService.reissue(any()))
                .willReturn(new ReissueResult("new.access.token", "new.refresh.token"));

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new ReissueRequest("refresh.token"))
                .when()
                .post("/api/auth/reissue")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("accessToken", equalTo("new.access.token"))
                .body("refreshToken", equalTo("new.refresh.token"));
    }

    @DisplayName("재발급 요청에 리프레시 토큰이 없으면 400 Bad Request를 응답한다.")
    @Test
    void reissue_blankRefreshToken() {
        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new ReissueRequest(""))
                .when()
                .post("/api/auth/reissue")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @DisplayName("폐기된 리프레시 토큰으로 재발급하면 401 Unauthorized를 응답한다.")
    @Test
    void reissue_revokedToken() {
        // given
        given(authService.reissue(any()))
                .willThrow(new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID));

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new ReissueRequest("revoked.refresh.token"))
                .when()
                .post("/api/auth/reissue")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AUTH_TOKEN_INVALID"));
    }

    @DisplayName("만료된 리프레시 토큰으로 재발급하면 401 Unauthorized를 응답한다.")
    @Test
    void reissue_expiredToken() {
        // given
        given(authService.reissue(any()))
                .willThrow(new BusinessException(AuthErrorCode.AUTH_TOKEN_EXPIRED));

        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new ReissueRequest("expired.refresh.token"))
                .when()
                .post("/api/auth/reissue")
                .then()
                .statusCode(HttpStatus.UNAUTHORIZED.value())
                .body("code", equalTo("AUTH_TOKEN_EXPIRED"));
    }

    @DisplayName("로그아웃에 성공하면 204 No Content를 응답한다.")
    @Test
    void logout() {
        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new LogoutRequest("refresh.token"))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @DisplayName("로그아웃 요청에 리프레시 토큰이 없으면 400 Bad Request를 응답한다.")
    @Test
    void logout_blankRefreshToken() {
        // when & then
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .body(new LogoutRequest(""))
                .when()
                .post("/api/auth/logout")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value());
    }
}