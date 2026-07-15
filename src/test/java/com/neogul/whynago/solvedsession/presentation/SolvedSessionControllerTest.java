package com.neogul.whynago.solvedsession.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionResult;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
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
    @DisplayName("본질문과 꼬리질문 풀이 결과를 한 번에 제출한다.")
    void create() {
        given(solvedSessionService.create(eq(10L), any())).willReturn(new CreateSolvedSessionResult(1L));

        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .queryParam("userId", 10L)
                .body("""
                        {
                          "rootQuestion": {"questionId": 1, "choiceId": 3, "relationQuestionId": 5},
                          "followupQuestions": [
                            {"questionId": 5, "choiceId": 11, "relationQuestionId": 8},
                            {"questionId": 8, "choiceId": 21, "relationQuestionId": null}
                          ]
                        }
                        """)
                .when()
                .post("/api/solved-sessions")
                .then()
                .statusCode(201)
                .body("sessionId", Matchers.equalTo(1));
    }

    @Test
    @DisplayName("rootQuestion이 없으면 400을 반환한다.")
    void createWithoutRootQuestion() {
        RestAssuredMockMvc.given()
                .contentType(ContentType.JSON)
                .queryParam("userId", 10L)
                .body("""
                        {
                          "followupQuestions": []
                        }
                        """)
                .when()
                .post("/api/solved-sessions")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }
}
