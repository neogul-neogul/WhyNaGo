package com.neogul.whynago.question.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.question.service.QuestionService.ChoiceResult;
import com.neogul.whynago.question.service.QuestionService.QuestionResult;
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

@WebMvcTest(QuestionController.class)
class QuestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuestionService questionService;

    @BeforeEach
    void setUp() {
        RestAssuredMockMvc.mockMvc(mockMvc);
    }

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
                        true,
                        List.of(new ChoiceResult(1L, "정답", 1, "", 2L)),
                        List.of("NETWORK")
                )
        ));

        RestAssuredMockMvc.given()
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
}
