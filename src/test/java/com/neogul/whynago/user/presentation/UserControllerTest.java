package com.neogul.whynago.user.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.service.dto.UserProfileResult;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class UserControllerTest extends ControllerTestSupport {

    @DisplayName("내 프로필(최소 학습 목표)을 조회한다.")
    @Test
    void getProfile() {
        given(userService.getProfile(10L)).willReturn(new UserProfileResult(15));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("dailyGoal", equalTo(15));
    }

    @DisplayName("최소 학습 목표를 수정한다.")
    @Test
    void updateDailyGoal() {
        given(userService.updateDailyGoal(10L, 20)).willReturn(new UserProfileResult(20));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        { "dailyGoal": 20 }
                        """)
                .when()
                .patch("/api/users/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("dailyGoal", equalTo(20));
    }

    @DisplayName("최소 학습 목표가 1 미만이면 400을 반환한다.")
    @Test
    void updateDailyGoal_invalid() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        { "dailyGoal": 0 }
                        """)
                .when()
                .patch("/api/users/me")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo("INVALID_INPUT"));
    }
}