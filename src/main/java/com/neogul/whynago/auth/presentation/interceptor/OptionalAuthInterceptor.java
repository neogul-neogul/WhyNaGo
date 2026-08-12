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

// 인증이 선택인 경로용. 토큰이 있으면 해석하고, 없으면 익명으로 통과시킨다.
@Component
@RequiredArgsConstructor
public class OptionalAuthInterceptor implements HandlerInterceptor {

    private final JwtProvider jwtProvider;
    private final TokenExtractor tokenExtractor;

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return true;
        }

        // 토큰을 보냈다면 만료·위조는 그대로 401로 알린다.
        JwtClaim claim = jwtProvider.parseToken(tokenExtractor.extractToken(authorizationHeader));
        request.setAttribute(AuthInterceptor.AUTH_CONTEXT_KEY, new AuthContext(claim.id()));
        return true;
    }
}
