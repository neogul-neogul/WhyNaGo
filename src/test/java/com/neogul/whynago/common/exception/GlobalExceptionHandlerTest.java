package com.neogul.whynago.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.user.exception.UserErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();
    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeEach
    void setUp() {
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionHandler.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
    }

    @DisplayName("4xx BusinessException은 info 로그를 남기고 해당 상태 코드로 응답한다.")
    @Test
    void handleBusinessException_clientError() {
        // when
        ResponseEntity<ErrorResponse> response =
                handler.handleBusinessException(new BusinessException(UserErrorCode.USER_NOT_FOUND));

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.INFO);
    }

    @DisplayName("5xx BusinessException은 warn 로그를 남기고 해당 상태 코드로 응답한다.")
    @Test
    void handleBusinessException_serverError() {
        // when
        ResponseEntity<ErrorResponse> response =
                handler.handleBusinessException(new BusinessException(QuestionErrorCode.ESSAY_AI_UNAVAILABLE));

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.WARN);
    }

    @DisplayName("제약 조건 위반은 info 로그를 남기고 400을 응답한다.")
    @Test
    void handleConstraintViolation() {
        // when
        ResponseEntity<ErrorResponse> response =
                handler.handleConstraintViolation(new ConstraintViolationException(Set.of()));

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.INFO);
    }

    @DisplayName("예상하지 못한 예외는 error 로그를 남기고 500을 응답한다.")
    @Test
    void handleException() {
        // when
        ResponseEntity<ErrorResponse> response = handler.handleException(new IllegalStateException("boom"));

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getLevel()).isEqualTo(Level.ERROR);
    }
}
