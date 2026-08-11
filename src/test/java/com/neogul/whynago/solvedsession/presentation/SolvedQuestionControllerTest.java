package com.neogul.whynago.solvedsession.presentation;

import static org.mockito.BDDMockito.given;

import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionIdsResult;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class SolvedQuestionControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("내가 푼 문제 ID 목록을 조회한다.")
    void findSolvedQuestionIds() {
        given(solvedQuestionService.readSolvedQuestionIds(10L))
                .willReturn(new SolvedQuestionIdsResult(List.of(100L, 101L)));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/solved-questions")
                .then()
                .statusCode(200)
                .body("questionIds", Matchers.contains(100, 101));
    }

    @Test
    @DisplayName("푼 문제가 없으면 빈 목록을 반환한다.")
    void findSolvedQuestionIds_noRecord() {
        given(solvedQuestionService.readSolvedQuestionIds(10L))
                .willReturn(new SolvedQuestionIdsResult(List.of()));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/solved-questions")
                .then()
                .statusCode(200)
                .body("questionIds", Matchers.empty());
    }

    @Test
    @DisplayName("인증 정보가 없으면 401을 반환한다.")
    void findSolvedQuestionIds_withoutToken() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/solved-questions")
                .then()
                .statusCode(401)
                .body("code", Matchers.equalTo("AUTH_TOKEN_MISSING"));
    }
}