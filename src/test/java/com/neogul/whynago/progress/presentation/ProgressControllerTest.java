package com.neogul.whynago.progress.presentation;

import static org.mockito.BDDMockito.given;

import com.neogul.whynago.progress.domain.Tier;
import com.neogul.whynago.progress.service.dto.CategoryProgressResult;
import com.neogul.whynago.progress.service.dto.ProgressDetailResult;
import com.neogul.whynago.progress.service.dto.ProgressSummaryResult;
import com.neogul.whynago.progress.service.dto.TierRange;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class ProgressControllerTest extends ControllerTestSupport {

    private static final List<TierRange> TIER_RANGES =
            Arrays.stream(Tier.values()).map(TierRange::from).toList();

    @Test
    @DisplayName("점수·티어와 카테고리별 전체/풀이/정답 문항 수를 조회한다.")
    void getProgress() {
        given(progressService.getDetail(10L)).willReturn(new ProgressDetailResult(
                90, Tier.SILVER, Tier.GOLD, 108, 15,
                List.of(
                        new CategoryProgressResult(Category.NETWORK, 40, 12, 5, 30),
                        new CategoryProgressResult(Category.LANGUAGE, 100, 5, 3, 8)
                ),
                TIER_RANGES, 700
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/progress")
                .then()
                .statusCode(200)
                .body("score", Matchers.equalTo(90))
                .body("tier", Matchers.equalTo("SILVER"))
                .body("nextTier", Matchers.equalTo("GOLD"))
                .body("scoreToNextTier", Matchers.equalTo(108))
                .body("totalQuestionCount", Matchers.equalTo(15))
                .body("categories[0].category", Matchers.equalTo("NETWORK"))
                .body("categories[0].totalCount", Matchers.equalTo(40))
                .body("categories[0].solvedCount", Matchers.equalTo(12))
                .body("categories[0].correctCount", Matchers.equalTo(5))
                .body("categories[0].score", Matchers.equalTo(30))
                .body("categories[1].category", Matchers.equalTo("LANGUAGE"))
                .body("categories[1].totalCount", Matchers.equalTo(100))
                .body("categories[1].correctCount", Matchers.equalTo(3))
                .body("maxScore", Matchers.equalTo(700))
                .body("tiers[0].tier", Matchers.equalTo("BRONZE"))
                .body("tiers[0].minScore", Matchers.equalTo(0))
                .body("tiers[4].tier", Matchers.equalTo("DIAMOND"))
                .body("tiers[4].minScore", Matchers.equalTo(677));
    }

    @Test
    @DisplayName("최고 티어이면 다음 티어 없이(null) 응답한다.")
    void getProgress_maxTier() {
        given(progressService.getDetail(10L)).willReturn(new ProgressDetailResult(
                690, Tier.DIAMOND, null, 0, 500, List.of(), TIER_RANGES, 700
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/progress")
                .then()
                .statusCode(200)
                .body("tier", Matchers.equalTo("DIAMOND"))
                .body("nextTier", Matchers.nullValue())
                .body("scoreToNextTier", Matchers.equalTo(0));
    }

    @Test
    @DisplayName("누적/연속 학습일·총 풀이 문제/정답/오답·1일1면접 횟수를 조회한다.")
    void getSummary() {
        given(progressService.getSummary(10L)).willReturn(new ProgressSummaryResult(42, 7, 128, 96, 32, 16));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/progress/summary")
                .then()
                .statusCode(200)
                .body("cumulativeDays", Matchers.equalTo(42))
                .body("streakDays", Matchers.equalTo(7))
                .body("totalQuestionCount", Matchers.equalTo(128))
                .body("totalCorrectCount", Matchers.equalTo(96))
                .body("totalWrongCount", Matchers.equalTo(32))
                .body("completedInterviewCount", Matchers.equalTo(16));
    }
}
