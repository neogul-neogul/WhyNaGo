package com.neogul.whynago.mastery.presentation;

import static org.mockito.BDDMockito.given;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.service.dto.CategoryMasteryResult;
import com.neogul.whynago.mastery.service.dto.MasteryResult;
import com.neogul.whynago.mastery.service.dto.TagMasteryResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.support.ControllerTestSupport;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class MasteryControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("카테고리별 숙련도 분포와 태그별 현재 숙련도·근거를 조회한다.")
    void getMastery() {
        given(masteryService.getMastery(10L)).willReturn(new MasteryResult(List.of(
                new CategoryMasteryResult(
                        Category.DB,
                        Map.of(MasteryLevel.NOT_LEARNED, 2L, MasteryLevel.SOLID, 1L),
                        List.of(new TagMasteryResult(
                                7L,
                                "인덱스",
                                MasteryLevel.NOT_LEARNED,
                                "카디널리티를 언급했지만 인덱스를 타지 않는 이유를 설명하지 못했다.",
                                LocalDateTime.of(2026, 8, 19, 10, 0)
                        ))
                )
        )));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/mastery")
                .then()
                .statusCode(200)
                .body("categories[0].category", Matchers.equalTo("DB"))
                .body("categories[0].levelCounts.NOT_LEARNED", Matchers.equalTo(2))
                .body("categories[0].levelCounts.SOLID", Matchers.equalTo(1))
                .body("categories[0].tags[0].tagId", Matchers.equalTo(7))
                .body("categories[0].tags[0].name", Matchers.equalTo("인덱스"))
                .body("categories[0].tags[0].level", Matchers.equalTo("NOT_LEARNED"))
                // 근거 없이 숙련도만 보여주면 사용자가 판정을 납득할 수 없다.
                .body("categories[0].tags[0].reason", Matchers.containsString("카디널리티"));
    }

    @Test
    @DisplayName("인증 없이 숙련도를 조회하면 실패한다.")
    void getMastery_withoutToken() {
        RestAssuredMockMvc.given()
                .when()
                .get("/api/mastery")
                .then()
                .statusCode(401);
    }
}
