package com.neogul.whynago.auth.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.fixture.LoginRequestFixture;
import com.neogul.whynago.auth.fixture.SignUpRequestFixture;
import com.neogul.whynago.auth.presentation.dto.LoginRequest;
import com.neogul.whynago.auth.presentation.dto.SignUpRequest;
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
}