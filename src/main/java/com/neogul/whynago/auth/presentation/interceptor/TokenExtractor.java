package com.neogul.whynago.auth.presentation.interceptor;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    // 헤더가 아예 없는 경우(비로그인)와 형식이 깨진 경우(변조)를 구분해, 클라이언트가 로그인 유도와
    // 재로그인 안내를 나눠 처리할 수 있게 한다.
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_MISSING);
        }
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}