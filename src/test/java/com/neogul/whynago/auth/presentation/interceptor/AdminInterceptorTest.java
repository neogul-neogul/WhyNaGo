package com.neogul.whynago.auth.presentation.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.domain.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AdminInterceptorTest {

    private final AdminInterceptor adminInterceptor = new AdminInterceptor();

    @DisplayName("관리자 권한이면 요청을 통과시킨다.")
    @Test
    void preHandle_admin() throws Exception {
        // given
        MockHttpServletRequest request = adminRequest(new AuthContext(1L, Role.ADMIN));

        // when
        boolean result = adminInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        // then
        assertThat(result).isTrue();
    }

    @DisplayName("일반 사용자 권한이면 권한 부족 예외가 발생한다.")
    @Test
    void preHandle_notAdmin() {
        // given
        MockHttpServletRequest request = adminRequest(new AuthContext(1L, Role.USER));

        // when & then
        assertThatThrownBy(() -> adminInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_FORBIDDEN));
    }

    @DisplayName("인증 정보가 해석되지 않았으면 권한 부족 예외가 발생한다.")
    @Test
    void preHandle_noAuthContext() {
        // given
        MockHttpServletRequest request = adminRequest(null);

        // when & then
        assertThatThrownBy(() -> adminInterceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_FORBIDDEN));
    }

    @DisplayName("CORS preflight 요청은 권한을 확인하지 않고 통과시킨다.")
    @Test
    void preHandle_preflight() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/admin/users");

        // when
        boolean result = adminInterceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        // then
        assertThat(result).isTrue();
    }

    private MockHttpServletRequest adminRequest(AuthContext authContext) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/admin/users");
        if (authContext != null) {
            request.setAttribute(AuthInterceptor.AUTH_CONTEXT_KEY, authContext);
        }
        return request;
    }
}
