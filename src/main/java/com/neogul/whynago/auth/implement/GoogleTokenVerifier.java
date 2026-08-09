package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.implement.dto.GoogleAccount;
import com.neogul.whynago.auth.infra.GoogleIdTokenClient;
import com.neogul.whynago.auth.infra.dto.GoogleUserInfo;
import com.neogul.whynago.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoogleTokenVerifier {

    private final GoogleIdTokenClient googleIdTokenClient;

    public GoogleAccount verify(String credential) {
        GoogleUserInfo userInfo = googleIdTokenClient.verify(credential)
                .orElseThrow(() -> new BusinessException(AuthErrorCode.AUTH_OAUTH_TOKEN_INVALID));
        if (!userInfo.emailVerified()) {
            throw new BusinessException(AuthErrorCode.AUTH_OAUTH_TOKEN_INVALID);
        }
        return new GoogleAccount(userInfo.sub(), userInfo.email());
    }
}