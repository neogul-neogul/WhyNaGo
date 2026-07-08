package com.neogul.whynago.auth.presentation.interceptor;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.common.exception.BusinessException;
import org.springframework.stereotype.Component;

@Component
public class TokenExtractor {

    private static final String BEARER_PREFIX = "Bearer ";

    public String extractToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
        return authorizationHeader.substring(BEARER_PREFIX.length());
    }
}