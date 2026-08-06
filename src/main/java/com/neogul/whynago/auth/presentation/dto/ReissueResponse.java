package com.neogul.whynago.auth.presentation.dto;

import com.neogul.whynago.auth.service.dto.ReissueResult;

public record ReissueResponse(String accessToken, String refreshToken) {

    public static ReissueResponse from(ReissueResult result) {
        return new ReissueResponse(result.accessToken(), result.refreshToken());
    }
}