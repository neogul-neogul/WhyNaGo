package com.neogul.whynago.user.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.Position;
import com.neogul.whynago.user.service.dto.UserProfileResult;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class UserControllerTest extends ControllerTestSupport {

    @DisplayName("내 프로필을 조회한다.")
    @Test
    void getProfile() {
        given(userService.getProfile(10L)).willReturn(
                new UserProfileResult("tester", "member@example.com", Position.BACKEND, 15, "소개"));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("nickname", equalTo("tester"))
                .body("email", equalTo("member@example.com"))
                .body("position", equalTo("BACKEND"))
                .body("dailyGoal", equalTo(15))
                .body("bio", equalTo("소개"));
    }

    @DisplayName("프로필을 수정한다.")
    @Test
    void updateProfile() {
        given(userService.updateProfile(eq(10L), any())).willReturn(
                new UserProfileResult("changed", "changed@example.com", Position.FRONTEND, 20, "새 소개"));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nickname": "changed",
                          "email": "changed@example.com",
                          "position": "FRONTEND",
                          "dailyGoal": 20,
                          "bio": "새 소개"
                        }
                        """)
                .when()
                .patch("/api/users/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("nickname", equalTo("changed"))
                .body("position", equalTo("FRONTEND"))
                .body("dailyGoal", equalTo(20));
    }

    @DisplayName("최소 학습 목표가 1 미만이면 400을 반환한다.")
    @Test
    void updateProfile_invalidDailyGoal() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nickname": "tester",
                          "email": "member@example.com",
                          "position": "BACKEND",
                          "dailyGoal": 0,
                          "bio": ""
                        }
                        """)
                .when()
                .patch("/api/users/me")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo("INVALID_INPUT"));
    }

    @DisplayName("닉네임 길이가 올바르지 않으면 400을 반환한다.")
    @Test
    void updateProfile_invalidNickname() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "nickname": "ab",
                          "email": "member@example.com",
                          "position": "BACKEND",
                          "dailyGoal": 10,
                          "bio": ""
                        }
                        """)
                .when()
                .patch("/api/users/me")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo("INVALID_INPUT"));
    }
}
