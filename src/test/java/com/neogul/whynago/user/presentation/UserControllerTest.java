package com.neogul.whynago.user.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.service.dto.UserProfileResult;
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
}