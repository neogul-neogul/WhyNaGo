package com.neogul.whynago.recommendation.presentation;

import static org.mockito.BDDMockito.given;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.recommendation.service.dto.RecommendationResult;
import com.neogul.whynago.recommendation.service.dto.RecommendedQuestionResult;
import com.neogul.whynago.recommendation.service.dto.WeakTagResult;
import com.neogul.whynago.recommendation.service.dto.WeakTagsResult;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class RecommendationControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("전체 이력 기반 취약 태그와 표본 수를 내려준다.")
    void weakTags() {
        given(recommendationService.weakTags(10L)).willReturn(new WeakTagsResult(
                8,
                List.of(new WeakTagResult("TCP/IP", 0.85, 3))
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/recommendations/weak-tags")
                .then()
                .statusCode(200)
                .body("sampleCount", Matchers.equalTo(8))
                .body("tags[0].tag", Matchers.equalTo("TCP/IP"))
                .body("tags[0].weaknessScore", Matchers.equalTo(0.85f))
                .body("tags[0].sampleCount", Matchers.equalTo(3));
    }

    @Test
    @DisplayName("인증 없이 취약 태그를 조회하면 실패한다.")
    void weakTags_withoutToken() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/recommendations/weak-tags")
                .then()
                .statusCode(401);
    }

    @Test
    @DisplayName("약점에 맞춰 생성된 문항과 함께 개인화·생성 여부를 내려준다.")
    void recommendQuestions() {
        given(recommendationService.recommend(10L)).willReturn(RecommendationResult.of(
                List.of(new RecommendedQuestionResult(
                        1L,
                        "인덱스와 카디널리티",
                        "카디널리티가 낮은 컬럼에 인덱스를 걸면 어떤 일이 벌어지는지 설명하라.",
                        QuestionType.ESSAY,
                        Difficulty.LOW,
                        Category.DB,
                        List.of("인덱스"),
                        true
                )),
                true
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/recommendations/questions")
                .then()
                .statusCode(200)
                .body("personalized", Matchers.equalTo(true))
                .body("generated", Matchers.equalTo(true))
                .body("questions[0].id", Matchers.equalTo(1))
                .body("questions[0].type", Matchers.equalTo("ESSAY"))
                .body("questions[0].difficulty", Matchers.equalTo("LOW"))
                .body("questions[0].category", Matchers.equalTo("DB"))
                .body("questions[0].tags[0]", Matchers.equalTo("인덱스"))
                .body("questions[0].generated", Matchers.equalTo(true));
    }

    @Test
    @DisplayName("콜드스타트 응답은 개인화·생성 여부를 모두 false로 내려준다.")
    void recommendQuestions_coldStart() {
        given(recommendationService.recommend(10L)).willReturn(RecommendationResult.of(
                List.of(new RecommendedQuestionResult(
                        2L,
                        "TCP와 UDP의 핵심 차이",
                        "TCP와 UDP의 가장 핵심적인 차이로 옳은 것은?",
                        QuestionType.MULTIPLE_CHOICE,
                        Difficulty.LOW,
                        Category.NETWORK,
                        List.of(),
                        false
                )),
                false
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/recommendations/questions")
                .then()
                .statusCode(200)
                .body("personalized", Matchers.equalTo(false))
                .body("generated", Matchers.equalTo(false));
    }

    @Test
    @DisplayName("인증 없이 추천을 조회하면 실패한다.")
    void recommendQuestions_withoutToken() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/recommendations/questions")
                .then()
                .statusCode(401);
    }
}
