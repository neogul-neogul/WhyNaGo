package com.neogul.whynago.admin.presentation;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.admin.service.dto.ChoiceDistributionResult;
import com.neogul.whynago.admin.service.dto.MultipleChoiceStatisticsResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.Role;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AdminQuestionStatisticsControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("관리자가 객관식 문제 통계를 조회하면 200과 지표를 응답한다.")
    void findMultipleChoiceStatistics() {
        given(adminQuestionStatisticsService.readMultipleChoiceStatistics(1L)).willReturn(statisticsResult());

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/1/statistics")
                .then()
                .statusCode(200)
                .body("questionId", Matchers.equalTo(1))
                .body("totalSolveCount", Matchers.equalTo(4))
                .body("correctCount", Matchers.equalTo(3))
                .body("correctRate", Matchers.equalTo(75.0f))
                .body("mostChosenChoice.choiceId", Matchers.equalTo(12))
                .body("mostChosenChoice.sequence", Matchers.equalTo(2))
                .body("mostChosenChoice.correct", Matchers.equalTo(true))
                .body("choiceDistribution", Matchers.hasSize(4))
                .body("choiceDistribution[0].selectedCount", Matchers.equalTo(1))
                .body("choiceDistribution[0].selectedRate", Matchers.equalTo(25.0f));
    }

    @Test
    @DisplayName("풀이가 없는 문제를 조회하면 가장 많이 고른 선택지가 null이다.")
    void findMultipleChoiceStatistics_noRecord() {
        given(adminQuestionStatisticsService.readMultipleChoiceStatistics(1L)).willReturn(
                new MultipleChoiceStatisticsResult(1L, 0L, 0L, 0.0, null, List.of())
        );

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/1/statistics")
                .then()
                .statusCode(200)
                .body("totalSolveCount", Matchers.equalTo(0))
                .body("mostChosenChoice", Matchers.nullValue());
    }

    @Test
    @DisplayName("일반 사용자가 문제 통계를 조회하면 403을 응답한다.")
    void findMultipleChoiceStatistics_notAdmin() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/admin/questions/1/statistics")
                .then()
                .statusCode(403)
                .body("code", Matchers.equalTo("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("서술형 문제의 통계를 조회하면 400을 응답한다.")
    void findMultipleChoiceStatistics_essayQuestion() {
        willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_MULTIPLE_CHOICE))
                .given(adminQuestionStatisticsService).readMultipleChoiceStatistics(1L);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/1/statistics")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("QUESTION_NOT_MULTIPLE_CHOICE"));
    }

    @Test
    @DisplayName("존재하지 않는 문제의 통계를 조회하면 404를 응답한다.")
    void findMultipleChoiceStatistics_notFound() {
        willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND))
                .given(adminQuestionStatisticsService).readMultipleChoiceStatistics(999L);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/999/statistics")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("QUESTION_NOT_FOUND"));
    }

    private MultipleChoiceStatisticsResult statisticsResult() {
        ChoiceDistributionResult mostChosen =
                new ChoiceDistributionResult(12L, 2, "Phantom Read", true, 3L, 75.0);

        return new MultipleChoiceStatisticsResult(
                1L,
                4L,
                3L,
                75.0,
                mostChosen,
                List.of(
                        new ChoiceDistributionResult(11L, 1, "Dirty Read", false, 1L, 25.0),
                        mostChosen,
                        new ChoiceDistributionResult(13L, 3, "Lost Update", false, 0L, 0.0),
                        new ChoiceDistributionResult(14L, 4, "Non-Repeatable Read", false, 0L, 0.0)
                )
        );
    }
}
