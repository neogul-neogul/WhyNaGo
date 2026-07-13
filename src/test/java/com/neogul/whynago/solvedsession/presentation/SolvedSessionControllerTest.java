package com.neogul.whynago.solvedsession.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.ScoredAnswer;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionResult;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SolvedSessionController.class)
class SolvedSessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolvedSessionService solvedSessionService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

    @Test
    @DisplayName("전체 풀이 결과를 한 번에 제출한다.")
    void submit() {
        given(solvedSessionService.submit(eq(10L), any())).willReturn(new SubmitSessionResult(
                1L,
                2,
                1,
                1,
                50.0,
                SessionStatus.COMPLETED,
                List.of(new ScoredAnswer(1L, 1L, 1L, true))
        ));

        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", 10L)
                .body("""
                        {
                          "rootQuestionId": 1,
                          "source": "PROBLEM_SOLVING",
                          "completed": true,
                          "answers": [
                            {"questionId": 1, "choiceId": 1},
                            {"questionId": 2, "choiceId": 6}
                          ]
                        }
                        """)
                .when()
                .post("/api/solved-sessions")
                .then()
                .statusCode(201)
                .body("sessionId", Matchers.equalTo(1))
                .body("totalCount", Matchers.equalTo(2))
                .body("correctRate", Matchers.equalTo(50.0f))
                .body("status", Matchers.equalTo("COMPLETED"))
                .body("items[0].correct", Matchers.equalTo(true));
    }

    @Test
    @DisplayName("answers가 비어 있으면 400을 반환한다.")
    void submitWithEmptyAnswers() {
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .header("X-User-Id", 10L)
                .body("""
                        {
                          "rootQuestionId": 1,
                          "source": "PROBLEM_SOLVING",
                          "completed": true,
                          "answers": []
                        }
                        """)
                .when()
                .post("/api/solved-sessions")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }
}
