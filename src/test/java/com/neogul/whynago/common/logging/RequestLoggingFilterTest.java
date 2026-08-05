package com.neogul.whynago.common.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.interceptor.AuthInterceptor;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class RequestLoggingFilterTest {

    private final RequestLoggingFilter filter = new RequestLoggingFilter();
    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    @BeforeEach
    void setUp() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(RequestLoggingFilter.class);
        logger.detachAppender(appender);
    }

    @DisplayName("인증된 요청은 userId를 포함한 로그를 남긴다.")
    @Test
    void doFilterInternal() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/questions");
        request.setAttribute(AuthInterceptor.AUTH_CONTEXT_KEY, new AuthContext(1L));
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage())
                .contains("GET")
                .contains("/api/questions")
                .contains("status=200")
                .contains("userId=1");
    }

    @DisplayName("인증되지 않은 요청은 userId 없이 로그를 남긴다.")
    @Test
    void doFilterInternal_unauthenticated() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/auth/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setStatus(200);
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage()).contains("userId=null");
    }

    @DisplayName("체인에서 예외가 발생해도 로그를 남기고 예외를 다시 던진다.")
    @Test
    void doFilterInternal_chainThrows() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/questions");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) -> {
            throw new IllegalStateException("boom");
        };

        // when // then
        assertThatThrownBy(() -> filter.doFilter(request, response, chain))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("boom");
        assertThat(appender.list).hasSize(1);
        assertThat(appender.list.get(0).getFormattedMessage()).contains("GET");
    }

    @DisplayName("요청마다 MDC에 requestId를 설정한다.")
    @Test
    void doFilterInternal_setsRequestIdInMdc() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/questions");
        MockHttpServletResponse response = new MockHttpServletResponse();
        String[] requestIdDuringChain = new String[1];
        FilterChain chain = (req, res) -> requestIdDuringChain[0] = MDC.get("requestId");

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(requestIdDuringChain[0]).isNotBlank();
    }

    @DisplayName("요청에 X-Request-Id 헤더가 있으면 그 값을 그대로 사용한다.")
    @Test
    void doFilterInternal_reusesGivenRequestId() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/questions");
        request.addHeader("X-Request-Id", "given-id");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(response.getHeader("X-Request-Id")).isEqualTo("given-id");
    }

    @DisplayName("처리 후에는 MDC를 정리한다.")
    @Test
    void doFilterInternal_clearsMdcAfterward() throws ServletException, IOException {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/questions");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        // when
        filter.doFilter(request, response, chain);

        // then
        assertThat(MDC.get("requestId")).isNull();
    }
}
