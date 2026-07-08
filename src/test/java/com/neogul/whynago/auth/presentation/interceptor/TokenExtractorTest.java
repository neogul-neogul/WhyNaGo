package com.neogul.whynago.auth.presentation.interceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TokenExtractorTest {

    private final TokenExtractor tokenExtractor = new TokenExtractor();

    @DisplayName("Authorization 헤더의 Bearer 토큰을 추출한다.")
    @Test
    void extractToken() {
        // given
        String authorizationHeader = "Bearer abc.def.ghi";

        // when
        String token = tokenExtractor.extractToken(authorizationHeader);

        // then
        assertThat(token).isEqualTo("abc.def.ghi");
    }

    @DisplayName("Authorization 헤더가 없으면 예외가 발생한다.")
    @Test
    void extractToken_noHeader() {
        // when & then
        assertThatThrownBy(() -> tokenExtractor.extractToken(null))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("Bearer 형식이 아니면 예외가 발생한다.")
    @Test
    void extractToken_notBearer() {
        // given
        String authorizationHeader = "abc.def.ghi";

        // when & then
        assertThatThrownBy(() -> tokenExtractor.extractToken(authorizationHeader))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }
}