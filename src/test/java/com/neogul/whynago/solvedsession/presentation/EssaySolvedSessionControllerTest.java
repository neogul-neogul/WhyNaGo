package com.neogul.whynago.solvedsession.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionResult;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class EssaySolvedSessionControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("본질문과 꼬리질문 문답 스냅샷을 하나의 서술형 세션으로 저장한다.")
    void create() {
        given(essaySolvedSessionService.create(eq(10L), any())).willReturn(new CreateEssaySolvedSessionResult(1L));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionId": 1, "questionText": "본질문", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true},
                          "followupQuestions": [
                            {"questionId": null, "questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false},
                            {"questionId": null, "questionText": "꼬리질문2", "userAnswer": "답변3", "feedback": "f3", "modelAnswer": "m3", "isCorrect": true}
                          ],
                          "startedAt": "2026-06-24T09:20:00"
                        }
                        """)
                .when()
                .post("/api/solved-sessions/essay")
                .then()
                .statusCode(201)
                .body("sessionId", Matchers.equalTo(1));
    }

    @Test
    @DisplayName("score가 10을 넘으면 400을 반환한다.")
    void createWithScoreOutOfRange() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionId": 1, "questionText": "본질문", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true, "score": 11},
                          "followupQuestions": [
                            {"questionId": null, "questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false},
                            {"questionId": null, "questionText": "꼬리질문2", "userAnswer": "답변3", "feedback": "f3", "modelAnswer": "m3", "isCorrect": true}
                          ],
                          "startedAt": "2026-06-24T09:20:00"
                        }
                        """)
                .when()
                .post("/api/solved-sessions/essay")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("rootQuestion이 없으면 400을 반환한다.")
    void createWithoutRootQuestion() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "followupQuestions": [
                            {"questionId": null, "questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false},
                            {"questionId": null, "questionText": "꼬리질문2", "userAnswer": "답변3", "feedback": "f3", "modelAnswer": "m3", "isCorrect": true}
                          ]
                        }
                        """)
                .when()
                .post("/api/solved-sessions/essay")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("꼬리질문 수가 2개가 아니면 400을 반환한다.")
    void createWithInvalidFollowupCount() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionId": 1, "questionText": "본질문", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true},
                          "followupQuestions": [
                            {"questionId": null, "questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false}
                          ]
                        }
                        """)
                .when()
                .post("/api/solved-sessions/essay")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("본질문 발문이 비어 있으면 400을 반환한다.")
    void createWithBlankQuestionText() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionId": 1, "questionText": " ", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true},
                          "followupQuestions": [
                            {"questionId": null, "questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false},
                            {"questionId": null, "questionText": "꼬리질문2", "userAnswer": "답변3", "feedback": "f3", "modelAnswer": "m3", "isCorrect": true}
                          ]
                        }
                        """)
                .when()
                .post("/api/solved-sessions/essay")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("startedAt이 없으면 400을 반환한다.")
    void createWithoutStartedAt() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "rootQuestion": {"questionId": 1, "questionText": "본질문", "userAnswer": "답변1", "feedback": "f1", "modelAnswer": "m1", "isCorrect": true},
                          "followupQuestions": [
                            {"questionId": null, "questionText": "꼬리질문1", "userAnswer": "답변2", "feedback": "f2", "modelAnswer": "m2", "isCorrect": false},
                            {"questionId": null, "questionText": "꼬리질문2", "userAnswer": "답변3", "feedback": "f3", "modelAnswer": "m3", "isCorrect": true}
                          ]
                        }
                        """)
                .when()
                .post("/api/solved-sessions/essay")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }
}
