package com.neogul.whynago.question.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.service.dto.ChoiceGradingResult;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.question.service.dto.QuestionResult;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class QuestionControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("문제 목록을 조회한다.")
    void findQuestions() {
        given(questionService.findQuestions(any())).willReturn(List.of(
                new QuestionResult(
                        1L,
                        "TCP와 UDP의 핵심 차이",
                        "내용",
                        QuestionType.MULTIPLE_CHOICE,
                        Difficulty.MEDIUM,
                        Category.NETWORK,
                        "해설",
                        List.of(new ChoiceResult(1L, "정답", 1, "", 2L)),
                        List.of("NETWORK")
                )
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .queryParam("type", "MULTIPLE_CHOICE")
                .queryParam("difficulty", "MEDIUM")
                .queryParam("category", "NETWORK")
                .queryParam("q", "TCP")
                .when()
                .get("/api/questions")
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(1))
                .body("[0].choices[0].relatedQuestionId", Matchers.equalTo(2))
                .body("[0].tags[0]", Matchers.equalTo("NETWORK"));
    }

    @Test
    @DisplayName("보기 선택 결과를 조회하면 채점 결과와 꼬리질문을 반환한다.")
    void getChoiceGrading() {
        given(questionService.getChoiceGrading(1L, 2L)).willReturn(new ChoiceGradingResult(
                false,
                1L,
                "정답 해설",
                "오답 사유",
                new QuestionResult(
                        5L,
                        "꼬리질문",
                        "내용",
                        QuestionType.MULTIPLE_CHOICE,
                        Difficulty.MEDIUM,
                        Category.NETWORK,
                        "해설",
                        List.of(new ChoiceResult(9L, "보기", 1, "", null)),
                        List.of("NETWORK")
                )
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/questions/1/choices/2")
                .then()
                .statusCode(200)
                .body("correct", Matchers.equalTo(false))
                .body("correctChoiceId", Matchers.equalTo(1))
                .body("explanation", Matchers.equalTo("정답 해설"))
                .body("choiceExplanation", Matchers.equalTo("오답 사유"))
                .body("nextQuestion.id", Matchers.equalTo(5))
                .body("nextQuestion.choices[0].id", Matchers.equalTo(9));
    }

    @Test
    @DisplayName("보기가 해당 문제에 속하지 않으면 400을 반환한다.")
    void getChoiceGrading_choiceNotInQuestion() {
        given(questionService.getChoiceGrading(1L, 2L))
                .willThrow(new BusinessException(QuestionErrorCode.CHOICE_NOT_IN_QUESTION));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/questions/1/choices/2")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("CHOICE_NOT_IN_QUESTION"));
    }

    @Test
    @DisplayName("문제가 존재하지 않으면 404를 반환한다.")
    void getChoiceGrading_questionNotFound() {
        given(questionService.getChoiceGrading(999L, 2L))
                .willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/questions/999/choices/2")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("QUESTION_NOT_FOUND"));
    }
}
