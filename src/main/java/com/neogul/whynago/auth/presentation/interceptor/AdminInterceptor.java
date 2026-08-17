package com.neogul.whynago.auth.presentation.interceptor;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.common.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

// 관리자 전용 경로용. 토큰 해석은 선행하는 AuthInterceptor가 끝냈으므로 권한만 확인한다.
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        AuthContext authContext = (AuthContext) request.getAttribute(AuthInterceptor.AUTH_CONTEXT_KEY);
        if (authContext == null || !authContext.isAdmin()) {
            throw new BusinessException(AuthErrorCode.AUTH_FORBIDDEN);
        }
        return true;
    }
}