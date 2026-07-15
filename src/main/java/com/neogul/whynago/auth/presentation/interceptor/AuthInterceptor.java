package com.neogul.whynago.auth.presentation.interceptor;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.implement.JwtProvider;
import com.neogul.whynago.auth.presentation.AuthContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    public static final String AUTH_CONTEXT_KEY = "authContext";

    private final JwtProvider jwtProvider;
    private final TokenExtractor tokenExtractor;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        if (HttpMethod.OPTIONS.matches(request.getMethod()) || !request.getRequestURI().startsWith("/api/")) {
            return true;
        }

        String token = tokenExtractor.extractToken(request.getHeader(HttpHeaders.AUTHORIZATION));
        JwtClaim claim = jwtProvider.parseToken(token);
        request.setAttribute(AUTH_CONTEXT_KEY, new AuthContext(claim.id()));
        return true;
    }
}