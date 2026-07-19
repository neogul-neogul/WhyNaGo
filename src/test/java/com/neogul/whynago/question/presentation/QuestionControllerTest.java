package com.neogul.whynago.question.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.question.service.dto.EssayQuestionResult;
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
    @DisplayName("서술형 본 질문을 조회하면 문항 정보와 태그를 응답한다.")
    void findEssayQuestion() {
        given(questionService.findEssayQuestion(3L)).willReturn(
                new EssayQuestionResult(
                        3L,
                        "TCP 흐름 제어 vs 혼잡 제어",
                        "TCP의 흐름 제어와 혼잡 제어의 차이를 설명하시오.",
                        QuestionType.ESSAY,
                        Difficulty.MEDIUM,
                        Category.NETWORK,
                        List.of("흐름 제어", "혼잡 제어")
                )
        );

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/questions/{questionId}/essay", 3L)
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(3))
                .body("title", Matchers.equalTo("TCP 흐름 제어 vs 혼잡 제어"))
                .body("type", Matchers.equalTo("ESSAY"))
                .body("difficulty", Matchers.equalTo("MEDIUM"))
                .body("category", Matchers.equalTo("NETWORK"))
                .body("tags", Matchers.contains("흐름 제어", "혼잡 제어"));
    }

    @Test
    @DisplayName("존재하지 않는 문제의 서술형 조회는 404를 응답한다.")
    void findEssayQuestion_questionNotFound() {
        given(questionService.findEssayQuestion(999L))
                .willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/questions/{questionId}/essay", 999L)
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("QUESTION_NOT_FOUND"));
    }

    @Test
    @DisplayName("서술형이 아닌 문제를 서술형으로 조회하면 400을 응답한다.")
    void findEssayQuestion_notEssay() {
        given(questionService.findEssayQuestion(1L))
                .willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_ESSAY));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/questions/{questionId}/essay", 1L)
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("QUESTION_NOT_ESSAY"));
    }
}
