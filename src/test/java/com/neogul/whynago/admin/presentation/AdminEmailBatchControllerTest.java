package com.neogul.whynago.admin.presentation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willThrow;

import com.neogul.whynago.admin.service.dto.EmailBatchExecutionDetailResult;
import com.neogul.whynago.admin.service.dto.EmailBatchExecutionResult;
import com.neogul.whynago.admin.service.dto.EmailBatchExecutionsResult;
import com.neogul.whynago.admin.service.dto.EmailSendLogResult;
import com.neogul.whynago.admin.service.dto.EmailSendLogSearchCommand;
import com.neogul.whynago.admin.service.dto.EmailSendLogsResult;
import com.neogul.whynago.admin.service.dto.FailureReasonResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.emailbatch.domain.EmailBatchStatus;
import com.neogul.whynago.emailbatch.domain.EmailSendStatus;
import com.neogul.whynago.emailbatch.exception.EmailBatchErrorCode;
import com.neogul.whynago.support.ControllerTestSupport;
import com.neogul.whynago.user.domain.Role;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import java.time.LocalDateTime;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpHeaders;

class AdminEmailBatchControllerTest extends ControllerTestSupport {

    @Test
    @DisplayName("관리자가 배치 실행 이력을 조회하면 200과 페이징된 이력을 반환한다.")
    void findExecutions() {
        given(adminEmailBatchListService.readExecutions(any())).willReturn(new EmailBatchExecutionsResult(
                List.of(execution()),
                0,
                4,
                45L
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .queryParam("page", 0)
                .queryParam("size", 4)
                .when()
                .get("/api/admin/email-batches")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("content[0].id", Matchers.equalTo(12))
                .body("content[0].executedAt", Matchers.equalTo("2026-08-19T21:00:00"))
                .body("content[0].totalTargetCount", Matchers.equalTo(340))
                .body("content[0].successCount", Matchers.equalTo(338))
                .body("content[0].failureCount", Matchers.equalTo(2))
                .body("content[0].status", Matchers.equalTo("PARTIAL_FAILURE"))
                .body("page", Matchers.equalTo(0))
                .body("size", Matchers.equalTo(4))
                .body("totalElements", Matchers.equalTo(45));
    }

    @Test
    @DisplayName("관리자가 배치 단건을 조회하면 200과 실패 사유 요약을 함께 반환한다.")
    void findExecution() {
        given(adminEmailBatchDetailService.readExecution(12L)).willReturn(new EmailBatchExecutionDetailResult(
                12L,
                LocalDateTime.of(2026, 8, 19, 21, 0),
                340,
                338,
                2,
                EmailBatchStatus.PARTIAL_FAILURE,
                List.of(new FailureReasonResult("Mailbox full", 2L))
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/email-batches/12")
                .then()
                .statusCode(200)
                .body("id", Matchers.equalTo(12))
                .body("totalTargetCount", Matchers.equalTo(340))
                .body("status", Matchers.equalTo("PARTIAL_FAILURE"))
                .body("failureReasons", Matchers.hasSize(1))
                .body("failureReasons[0].reason", Matchers.equalTo("Mailbox full"))
                .body("failureReasons[0].count", Matchers.equalTo(2));
    }

    @Test
    @DisplayName("존재하지 않는 배치를 조회하면 404를 응답한다.")
    void findExecution_notFound() {
        willThrow(new BusinessException(EmailBatchErrorCode.EMAIL_BATCH_NOT_FOUND))
                .given(adminEmailBatchDetailService).readExecution(999L);

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .when()
                .get("/api/admin/email-batches/999")
                .then()
                .statusCode(404)
                .body("code", Matchers.equalTo("EMAIL_BATCH_NOT_FOUND"));
    }

    @Test
    @DisplayName("관리자가 발송 상세 목록을 조회하면 200과 수신자별 발송 결과를 반환한다.")
    void findSendLogs() {
        given(adminEmailBatchDetailService.readSendLogs(any())).willReturn(new EmailSendLogsResult(
                List.of(new EmailSendLogResult(
                        981L,
                        501L,
                        "user@example.com",
                        LocalDateTime.of(2026, 8, 19, 21, 0, 3),
                        EmailSendStatus.FAILURE,
                        "Mailbox full"
                )),
                0,
                8,
                2L
        ));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .queryParam("status", "FAILURE")
                .queryParam("page", 0)
                .queryParam("size", 8)
                .when()
                .get("/api/admin/email-batches/12/send-logs")
                .then()
                .statusCode(200)
                .body("content", Matchers.hasSize(1))
                .body("content[0].userId", Matchers.equalTo(501))
                .body("content[0].recipientEmail", Matchers.equalTo("user@example.com"))
                .body("content[0].sentAt", Matchers.equalTo("2026-08-19T21:00:03"))
                .body("content[0].status", Matchers.equalTo("FAILURE"))
                .body("content[0].failureReason", Matchers.equalTo("Mailbox full"))
                .body("totalElements", Matchers.equalTo(2));
    }

    @Test
    @DisplayName("상태 쿼리 파라미터를 발송 상세 조회 조건으로 전달한다.")
    void findSendLogs_passesStatusFilter() {
        given(adminEmailBatchDetailService.readSendLogs(any()))
                .willReturn(new EmailSendLogsResult(List.of(), 0, 8, 0L));

        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L, Role.ADMIN))
                .queryParam("status", "FAILURE")
                .when()
                .get("/api/admin/email-batches/12/send-logs")
                .then()
                .statusCode(200);

        ArgumentCaptor<EmailSendLogSearchCommand> captor =
                ArgumentCaptor.forClass(EmailSendLogSearchCommand.class);
        then(adminEmailBatchDetailService).should().readSendLogs(captor.capture());
        assertThat(captor.getValue().status()).isEqualTo(EmailSendStatus.FAILURE);
        assertThat(captor.getValue().executionId()).isEqualTo(12L);
    }

    @Test
    @DisplayName("일반 사용자가 배치 실행 이력을 조회하면 403을 응답한다.")
    void findExecutions_notAdmin() {
        RestAssuredMockMvc.given()
                .header(HttpHeaders.AUTHORIZATION, bearerToken(1L))
                .when()
                .get("/api/admin/email-batches")
                .then()
                .statusCode(403)
                .body("code", Matchers.equalTo("AUTH_FORBIDDEN"));
    }

    private EmailBatchExecutionResult execution() {
        return new EmailBatchExecutionResult(
                12L,
                LocalDateTime.of(2026, 8, 19, 21, 0),
                340,
                338,
                2,
                EmailBatchStatus.PARTIAL_FAILURE
        );
    }
}
