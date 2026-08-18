package com.neogul.whynago.interview.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.interview.service.dto.AnswerInterviewResult;
import com.neogul.whynago.interview.service.dto.CompleteInterviewResult;
import com.neogul.whynago.interview.service.dto.InterviewFollowupResult;
import com.neogul.whynago.interview.service.dto.InterviewGradingResult;
import com.neogul.whynago.interview.service.dto.InterviewQuestionResult;
import com.neogul.whynago.interview.service.dto.InterviewResultDetail;
import com.neogul.whynago.interview.service.dto.InterviewResultItemDetail;
import com.neogul.whynago.interview.service.dto.InterviewSummaryResult;
import com.neogul.whynago.interview.service.dto.StartInterviewResult;
import com.neogul.whynago.interview.service.dto.TodayInterviewResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class InterviewControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("오늘의 면접 상태를 조회한다.")
    void getTodayStatus() {
        given(interviewService.getTodayStatus(10L)).willReturn(new TodayInterviewResult("AVAILABLE", null));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/interviews/today")
                .then()
                .statusCode(200)
                .body("status", Matchers.equalTo("AVAILABLE"))
                .body("interviewId", Matchers.nullValue());
    }

    @Test
    @DisplayName("면접을 시작하면 201과 오늘의 질문을 반환한다.")
    void start() {
        given(interviewService.start(10L)).willReturn(new StartInterviewResult(
                1L,
                new InterviewQuestionResult(7L, "제목", "발문", Category.NETWORK, Difficulty.MEDIUM),
                3,
                180,
                LocalDateTime.of(2026, 8, 7, 9, 20, 0)
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .post("/api/interviews")
                .then()
                .statusCode(201)
                .body("interviewId", Matchers.equalTo(1))
                .body("question.id", Matchers.equalTo(7))
                .body("question.content", Matchers.equalTo("발문"))
                .body("totalQuestionCount", Matchers.equalTo(3))
                .body("timeLimitSeconds", Matchers.equalTo(180));
    }

    @Test
    @DisplayName("답변을 제출하면 채점 결과와 꼬리질문을 반환한다.")
    void answer() {
        given(interviewService.answer(eq(10L), eq(1L), any())).willReturn(new AnswerInterviewResult(
                new InterviewGradingResult("피드백", "모범답안", 8, true),
                new InterviewFollowupResult("꼬리질문1")
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {"question": "본질문", "answer": "답변"}
                        """)
                .when()
                .post("/api/interviews/1/answers")
                .then()
                .statusCode(200)
                .body("grading.feedback", Matchers.equalTo("피드백"))
                .body("grading.isCorrect", Matchers.equalTo(true))
                .body("grading.score", Matchers.equalTo(8))
                .body("nextFollowup.question", Matchers.equalTo("꼬리질문1"));
    }

    @Test
    @DisplayName("타이머 만료로 빈 답변이 와도 채점 요청을 받아들인다.")
    void answerWithBlankAnswer() {
        given(interviewService.answer(eq(10L), eq(1L), any())).willReturn(new AnswerInterviewResult(
                new InterviewGradingResult("피드백", "모범답안", 4, false),
                null
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {"question": "본질문", "answer": ""}
                        """)
                .when()
                .post("/api/interviews/1/answers")
                .then()
                .statusCode(200)
                .body("nextFollowup", Matchers.nullValue());
    }

    @Test
    @DisplayName("발문이 비어 있으면 400을 반환한다.")
    void answerWithBlankQuestion() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {"question": "  ", "answer": "답변"}
                        """)
                .when()
                .post("/api/interviews/1/answers")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("면접을 완료하면 201과 저장된 세션 ID를 반환한다.")
    void complete() {
        given(interviewService.complete(eq(10L), eq(1L), any()))
                .willReturn(new CompleteInterviewResult(1L, 42L));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body(completeBody())
                .when()
                .post("/api/interviews/1/complete")
                .then()
                .statusCode(201)
                .body("interviewId", Matchers.equalTo(1))
                .body("solvedSessionId", Matchers.equalTo(42));
    }

    @Test
    @DisplayName("완료 요청의 빈 답변은 저장을 막지 않는다.")
    void completeWithBlankUserAnswer() {
        given(interviewService.complete(eq(10L), eq(1L), any()))
                .willReturn(new CompleteInterviewResult(1L, 42L));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionText": "본질문", "userAnswer": "", "feedback": "f1", "modelAnswer": "m1", "isCorrect": false},
                          "followupQuestions": [
                            {"questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": true},
                            {"questionText": "꼬리질문2", "userAnswer": "", "feedback": "f3", "modelAnswer": "m3", "isCorrect": false}
                          ],
                          "focusLossCount": 0
                        }
                        """)
                .when()
                .post("/api/interviews/1/complete")
                .then()
                .statusCode(201);
    }

    @Test
    @DisplayName("꼬리질문 수가 2개가 아니면 400을 반환한다.")
    void completeWithInvalidFollowupCount() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionText": "본질문", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true},
                          "followupQuestions": [
                            {"questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": true}
                          ],
                          "focusLossCount": 0
                        }
                        """)
                .when()
                .post("/api/interviews/1/complete")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("면접을 취소하면 204를 반환한다.")
    void cancel() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .delete("/api/interviews/1")
                .then()
                .statusCode(204);
    }

    @Test
    @DisplayName("면접 결과를 문항별로 조회한다.")
    void findResult() {
        given(interviewService.findResult(10L, 1L)).willReturn(new InterviewResultDetail(
                1L,
                LocalDate.of(2026, 8, 7),
                "COMPLETED",
                Category.NETWORK,
                3,
                2,
                2,
                LocalDateTime.of(2026, 8, 7, 9, 20, 0),
                LocalDateTime.of(2026, 8, 7, 9, 31, 40),
                700L,
                List.of(new InterviewResultItemDetail(1, "MAIN", "본질문", "답변1", "f1", "m1", true))
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/interviews/1")
                .then()
                .statusCode(200)
                .body("status", Matchers.equalTo("COMPLETED"))
                .body("correctCount", Matchers.equalTo(2))
                .body("durationSeconds", Matchers.equalTo(700))
                .body("items[0].type", Matchers.equalTo("MAIN"))
                .body("items[0].modelAnswer", Matchers.equalTo("m1"));
    }

    @Test
    @DisplayName("면접 기록 목록을 조회한다.")
    void findAll() {
        given(interviewService.findAll(10L)).willReturn(List.of(new InterviewSummaryResult(
                7L,
                LocalDate.of(2026, 8, 7),
                Category.NETWORK,
                "TCP 흐름 제어",
                3,
                2,
                LocalDateTime.of(2026, 8, 7, 9, 16, 41)
        )));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/interviews")
                .then()
                .statusCode(200)
                .body("[0].interviewId", Matchers.equalTo(7))
                .body("[0].category", Matchers.equalTo("NETWORK"))
                .body("[0].title", Matchers.equalTo("TCP 흐름 제어"))
                .body("[0].totalCount", Matchers.equalTo(3))
                .body("[0].correctCount", Matchers.equalTo(2));
    }

    @Test
    @DisplayName("완료된 면접이 없으면 빈 목록을 반환한다.")
    void findAllWhenEmpty() {
        given(interviewService.findAll(10L)).willReturn(List.of());

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/interviews")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(0));
    }

    private String completeBody() {
        return """
                {
                  "rootQuestion": {"questionText": "본질문", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true},
                  "followupQuestions": [
                    {"questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false},
                    {"questionText": "꼬리질문2", "userAnswer": "답변3", "feedback": "f3", "modelAnswer": "m3", "isCorrect": true}
                  ],
                  "focusLossCount": 2
                }
                """;
    }
}
