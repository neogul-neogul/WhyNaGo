package com.neogul.whynago.notification.presentation;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.notification.service.dto.NotificationSettingResult;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

class NotificationSettingControllerTest extends ControllerTestSupport {

    @DisplayName("내 알림 설정을 조회한다.")
    @Test
    void getSettings() {
        given(notificationSettingService.getSettings(10L)).willReturn(new NotificationSettingResult(true));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/notification-settings/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("everyDayRemind", equalTo(true));
    }

    @DisplayName("알림 설정을 수정한다.")
    @Test
    void updateSettings() {
        given(notificationSettingService.updateSettings(eq(10L), any())).willReturn(
                new NotificationSettingResult(false));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "everyDayRemind": false
                        }
                        """)
                .when()
                .patch("/api/notification-settings/me")
                .then()
                .statusCode(HttpStatus.OK.value())
                .body("everyDayRemind", equalTo(false));
    }
}
