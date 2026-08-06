package com.neogul.whynago.auth.service.dto;

import com.neogul.whynago.auth.domain.TokenPair;

public record ReissueResult(String accessToken, String refreshToken) {

    public static ReissueResult from(TokenPair tokenPair) {
        return new ReissueResult(tokenPair.accessToken(), tokenPair.refreshToken());
    }
}