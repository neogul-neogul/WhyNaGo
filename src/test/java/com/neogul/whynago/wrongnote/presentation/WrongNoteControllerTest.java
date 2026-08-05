package com.neogul.whynago.wrongnote.presentation;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.wrongnote.exception.WrongNoteErrorCode;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteBookmarkResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteChoiceResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteDetailResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteEssayItemResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteMultipleChoiceItemResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteSummaryResult;
import io.restassured.http.ContentType;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;

class WrongNoteControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("오답노트 목록을 조회한다.")
    void findAll() {
        given(wrongNoteService.findAll(eq(10L), isNull())).willReturn(List.of(new WrongNoteSummaryResult(
                1L, 7L, QuestionType.MULTIPLE_CHOICE, Category.NETWORK, Difficulty.MEDIUM,
                "TCP 3-way handshake", true, LocalDateTime.of(2026, 6, 25, 10, 0)
        )));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/wrong-notes")
                .then()
                .statusCode(200)
                .body("[0].id", Matchers.equalTo(1))
                .body("[0].questionId", Matchers.equalTo(7))
                .body("[0].type", Matchers.equalTo("MULTIPLE_CHOICE"))
                .body("[0].isBookmarked", Matchers.equalTo(true));
    }

    @Test
    @DisplayName("북마크 필터로 오답노트 목록을 조회한다.")
    void findAll_bookmarked() {
        given(wrongNoteService.findAll(10L, true)).willReturn(List.of());

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .queryParam("bookmarked", true)
                .when()
                .get("/api/wrong-notes")
                .then()
                .statusCode(200)
                .body("size()", Matchers.equalTo(0));
    }

    @Test
    @DisplayName("객관식 오답노트 상세를 조회한다.")
    void findDetail_multipleChoice() {
        WrongNoteDetailResult result = new WrongNoteDetailResult(
                1L, QuestionType.MULTIPLE_CHOICE, Category.NETWORK, Difficulty.MEDIUM, false,
                LocalDateTime.of(2026, 6, 25, 10, 0),
                List.of(new WrongNoteMultipleChoiceItemResult(
                        1, 1L, "TCP 3-way handshake", "지문",
                        List.of(new WrongNoteChoiceResult(10L, "SYN → ACK → SYN-ACK", 1, false),
                                new WrongNoteChoiceResult(11L, "SYN → SYN-ACK → ACK", 2, true)),
                        10L, 11L, false, "정답 해설", "오답 해설"
                )),
                null
        );
        given(wrongNoteService.findDetail(10L, 1L)).willReturn(result);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/wrong-notes/1")
                .then()
                .statusCode(200)
                .body("type", Matchers.equalTo("MULTIPLE_CHOICE"))
                .body("essayItems", Matchers.nullValue())
                .body("multipleChoiceItems[0].choices.size()", Matchers.equalTo(2))
                .body("multipleChoiceItems[0].choiceExplanation", Matchers.equalTo("오답 해설"));
    }

    @Test
    @DisplayName("서술형 오답노트 상세를 조회한다.")
    void findDetail_essay() {
        WrongNoteDetailResult result = new WrongNoteDetailResult(
                2L, QuestionType.ESSAY, Category.DB, Difficulty.HIGH, false,
                LocalDateTime.of(2026, 6, 24, 9, 30),
                null,
                List.of(new WrongNoteEssayItemResult(1, "격리 수준을 설명하라.", "답변1", "피드백1", "모범답안1", true))
        );
        given(wrongNoteService.findDetail(10L, 2L)).willReturn(result);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/wrong-notes/2")
                .then()
                .statusCode(200)
                .body("type", Matchers.equalTo("ESSAY"))
                .body("multipleChoiceItems", Matchers.nullValue())
                .body("essayItems[0].modelAnswer", Matchers.equalTo("모범답안1"));
    }

    @Test
    @DisplayName("존재하지 않는 오답노트를 조회하면 404를 반환한다.")
    void findDetail_notFound() {
        given(wrongNoteService.findDetail(10L, 999L))
                .willThrow(new BusinessException(WrongNoteErrorCode.WRONG_NOTE_NOT_FOUND));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .get("/api/wrong-notes/999")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("WRONG_NOTE_NOT_FOUND"));
    }

    @Test
    @DisplayName("오답노트 북마크 상태를 변경한다.")
    void updateBookmark() {
        given(wrongNoteService.updateBookmark(10L, 1L, true)).willReturn(new WrongNoteBookmarkResult(true));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("""
                        { "bookmarked": true }
                        """)
                .when()
                .patch("/api/wrong-notes/1/bookmark")
                .then()
                .statusCode(200)
                .body("isBookmarked", Matchers.equalTo(true));
    }

    @Test
    @DisplayName("bookmarked 값이 없으면 400을 반환한다.")
    void updateBookmark_missingValue() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .patch("/api/wrong-notes/1/bookmark")
                .then()
                .statusCode(400)
                .body("code", Matchers.equalTo("INVALID_INPUT"));
    }

    @Test
    @DisplayName("오답노트를 삭제하면 204를 반환한다.")
    void delete() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(10L))
                .when()
                .delete("/api/wrong-notes/1")
                .then()
                .statusCode(204);
    }
}
