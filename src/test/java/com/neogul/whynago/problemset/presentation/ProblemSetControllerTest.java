package com.neogul.whynago.problemset.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.problemset.exception.ProblemSetErrorCode;
import com.neogul.whynago.problemset.service.dto.CreateProblemSetResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetDetailResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetItemResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetMembershipResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetSummaryResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class ProblemSetControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("문제집을 생성하면 201 Created를 응답한다.")
    void create() {
        given(problemSetService.create(any())).willReturn(
                new CreateProblemSetResult(1L, "면접 D-7 벼락치기", LocalDateTime.of(2026, 6, 25, 10, 0)));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        { "name": "면접 D-7 벼락치기" }
                        """)
                .when()
                .post("/api/problem-sets")
                .then()
                .statusCode(201)
                .body("id", Matchers.equalTo(1))
                .body("name", Matchers.equalTo("면접 D-7 벼락치기"));
    }

    @Test
    @DisplayName("이름이 비어 있으면 400을 반환한다.")
    void create_blankName() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        { "name": "" }
                        """)
                .when()
                .post("/api/problem-sets")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("문제집 목록을 조회한다.")
    void findAll() {
        given(problemSetService.findAll(10L)).willReturn(List.of(new ProblemSetSummaryResult(
                1L, "면접 D-7 벼락치기", 2, List.of("TCP와 UDP의 핵심 차이는?"), LocalDateTime.of(2026, 6, 25, 10, 0)
        )));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/problem-sets")
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(1))
                .body("[0].itemCount", Matchers.equalTo(2))
                .body("[0].previewTitles.size()", Matchers.equalTo(1));
    }

    @Test
    @DisplayName("문제집 상세를 조회한다.")
    void findDetail() {
        given(problemSetService.findDetail(10L, 1L)).willReturn(new ProblemSetDetailResult(
                1L, "면접 D-7 벼락치기", LocalDateTime.of(2026, 6, 25, 10, 0),
                List.of(new ProblemSetItemResult(7L, "TCP와 UDP의 핵심 차이는?", Category.NETWORK,
                        QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM))
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/problem-sets/1")
                .then()
                .statusCode(200)
                .body("name", Matchers.equalTo("면접 D-7 벼락치기"))
                .body("items[0].questionId", Matchers.equalTo(7))
                .body("items[0].category", Matchers.equalTo("NETWORK"));
    }

    @Test
    @DisplayName("존재하지 않는 문제집을 조회하면 404를 반환한다.")
    void findDetail_notFound() {
        given(problemSetService.findDetail(10L, 999L))
                .willThrow(new BusinessException(ProblemSetErrorCode.PROBLEM_SET_NOT_FOUND));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/problem-sets/999")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("PROBLEM_SET_NOT_FOUND"));
    }

    @Test
    @DisplayName("문제 저장 여부를 포함해 문제집 멤버십을 조회한다.")
    void findMembership() {
        given(problemSetService.findMembership(10L, 7L)).willReturn(List.of(
                new ProblemSetMembershipResult(1L, "면접 D-7 벼락치기", 2, true),
                new ProblemSetMembershipResult(2L, "네트워크 집중 보완", 1, false)
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .queryParam("questionId", 7)
                .when()
                .get("/api/problem-sets/membership")
                .then()
                .statusCode(200)
                .body("[0].saved", Matchers.equalTo(true))
                .body("[1].saved", Matchers.equalTo(false));
    }

    @Test
    @DisplayName("문제집에 문제를 담으면 204를 반환한다.")
    void addItem() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .put("/api/problem-sets/1/items/7")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("존재하지 않는 문제를 담으려 하면 404를 반환한다.")
    void addItem_questionNotFound() {
        willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND))
                .given(problemSetService).addItem(10L, 1L, 999L);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .put("/api/problem-sets/1/items/999")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("QUESTION_NOT_FOUND"));
    }

    @Test
    @DisplayName("문제집에서 문제를 빼면 204를 반환한다.")
    void removeItem() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .delete("/api/problem-sets/1/items/7")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("문제집을 삭제하면 204를 반환한다.")
    void delete() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .delete("/api/problem-sets/1")
                .then()
                .statusCode(204);
    }
}
