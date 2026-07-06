package com.neogul.whynago.auth.presentation.interceptor;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public static String extractToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
        return authorization.substring(BEARER_PREFIX.length());
    }
}