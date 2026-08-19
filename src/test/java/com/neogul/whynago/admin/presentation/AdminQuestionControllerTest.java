package com.neogul.whynago.admin.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.admin.service.dto.AdminChoiceResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionDetailResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionsResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.Role;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class AdminQuestionControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("관리자가 문제 목록을 조회하면 200과 페이지 응답을 반환한다.")
    void findQuestions() {
        given(adminQuestionListService.readQuestions(any())).willReturn(new AdminQuestionsResult(
                List.of(
                        new AdminQuestionResult(
                                1L, "REPEATABLE READ의 이상 현상", Category.DB, Difficulty.MEDIUM,
                                QuestionType.MULTIPLE_CHOICE, 1842L, 63.8
                        ),
                        new AdminQuestionResult(
                                2L, "SYN flooding이 성립하는 원인", Category.NETWORK, Difficulty.HIGH,
                                QuestionType.ESSAY, 0L, null
                        )
                ),
                0,
                20,
                2L
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(2))
                .body("content[0].id", Matchers.equalTo(1))
                .body("content[0].solveCount", Matchers.equalTo(1842))
                .body("content[0].correctRate", Matchers.equalTo(63.8f))
                .body("content[1].solveCount", Matchers.equalTo(0))
                .body("content[1].correctRate", Matchers.nullValue())
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(20))
                .body("totalElements", Matchers.equalTo(2));
    }

    @Test
    @DisplayName("일반 사용자가 문제 목록을 조회하면 403을 응답한다.")
    void findQuestions_notAdmin() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/admin/questions")
                .then()
                .statusCode(403)
                .body("code", Matchers.equalTo("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("관리자가 객관식 문제 상세를 조회하면 선택지에 정답 여부가 포함된다.")
    void findQuestion_multipleChoice() {
        AdminQuestionDetailResult result = new AdminQuestionDetailResult(
                12L,
                "REPEATABLE READ의 이상 현상",
                "트랜잭션 격리 수준을 REPEATABLE READ로 설정했을 때...",
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                Category.DB,
                "REPEATABLE READ는 동일 트랜잭션 내...",
                List.of(
                        new AdminChoiceResult(33L, 1, "Dirty Read", false, "READ UNCOMMITTED에서만 발생", null),
                        new AdminChoiceResult(34L, 2, "Phantom Read", true, "", null)
                ),
                List.of("트랜잭션", "격리수준"),
                null,
                null
        );
        given(adminQuestionDetailService.readQuestion(12L)).willReturn(result);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/12")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(12))
                .body("choices", Matchers.hasSize(2))
                .body("choices[0].correct", Matchers.equalTo(false))
                .body("choices[1].correct", Matchers.equalTo(true))
                .body("solveCount", Matchers.nullValue())
                .body("correctRate", Matchers.nullValue());
    }

    @Test
    @DisplayName("관리자가 서술형 문제 상세를 조회하면 선택지는 비어 있고 풀이수·정답률이 함께 내려온다.")
    void findQuestion_essay() {
        AdminQuestionDetailResult result = new AdminQuestionDetailResult(
                7L,
                "SYN flooding이 성립하는 원인",
                "TCP 3-way handshake 과정에서...",
                QuestionType.ESSAY,
                Difficulty.HIGH,
                Category.NETWORK,
                "서버가 SYN을 받은 뒤...",
                List.of(),
                List.of("TCP", "보안"),
                312L,
                41.2
        );
        given(adminQuestionDetailService.readQuestion(7L)).willReturn(result);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/7")
                .then()
                .statusCode(200)
                .body("choices", Matchers.hasSize(0))
                .body("solveCount", Matchers.equalTo(312))
                .body("correctRate", Matchers.equalTo(41.2f));
    }

    @Test
    @DisplayName("일반 사용자가 문제 상세를 조회하면 403을 응답한다.")
    void findQuestion_notAdmin() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/admin/questions/1")
                .then()
                .statusCode(403)
                .body("code", Matchers.equalTo("AUTH_FORBIDDEN"));
    }

    @Test
    @DisplayName("존재하지 않는 문제 상세를 조회하면 404를 응답한다.")
    void findQuestion_notFound() {
        willThrow(new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND))
                .given(adminQuestionDetailService).readQuestion(999L);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/questions/999")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("QUESTION_NOT_FOUND"));
    }
}
