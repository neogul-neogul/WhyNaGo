package com.neogul.whynago.notification.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.notification.service.dto.NotificationSettingResult;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class NotificationSettingControllerTest extends ControllerTestSupport {

    @DisplayName("내 알림 설정을 조회한다.")
    @Test
    void getSettings() {
        given(notificationSettingService.getSettings(10L)).willReturn(
                new NotificationSettingResult(true, LocalTime.of(21, 0), true, true, false, true));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/notification-settings/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("everyDayRemind", equalTo(true))
                .body("remindTime", equalTo("21:00:00"))
                .body("interviewRemind", equalTo(false));
    }

    @DisplayName("알림 설정을 수정한다.")
    @Test
    void updateSettings() {
        given(notificationSettingService.updateSettings(eq(10L), any())).willReturn(
                new NotificationSettingResult(false, LocalTime.of(8, 0), false, true, true, false));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "everyDayRemind": false,
                          "remindTime": "08:00:00",
                          "streakStopPrevention": false,
                          "wrongNote": true,
                          "interviewRemind": true,
                          "weeklyReport": false
                        }
                        """)
                .when()
                .patch("/api/notification-settings/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("everyDayRemind", equalTo(false))
                .body("remindTime", equalTo("08:00:00"))
                .body("interviewRemind", equalTo(true));
    }

    @DisplayName("알림 시간이 없으면 400을 반환한다.")
    @Test
    void updateSettings_missingRemindTime() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "everyDayRemind": true,
                          "streakStopPrevention": true,
                          "wrongNote": true,
                          "interviewRemind": false,
                          "weeklyReport": true
                        }
                        """)
                .when()
                .patch("/api/notification-settings/me")
                .then()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .body("code", equalTo("INVALID_INPUT"));
    }
}
