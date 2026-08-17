package com.neogul.whynago.auth.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.domain.Role;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JwtProviderTest {

    private static final String SECRET = "test-secret-key-for-jwt-provider-0123456789";
    private static final long ACCESS_EXPIRATION = 1_800_000L;
    private static final long REFRESH_EXPIRATION = 604_800_000L;

    private JwtProvider jwtProvider;

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, ACCESS_EXPIRATION, REFRESH_EXPIRATION);
    }

    @DisplayName("액세스 토큰을 발급한 뒤 파싱하면 담긴 사용자 id와 권한이 복원된다.")
    @Test
    void createAccessToken() {
        // given
        String token = jwtProvider.createAccessToken(new JwtClaim(1L, Role.ADMIN));

        // when
        JwtClaim parsed = jwtProvider.parseToken(token);

        // then
        assertThat(parsed.id()).isEqualTo(1L);
        assertThat(parsed.role()).isEqualTo(Role.ADMIN);
    }

    @DisplayName("권한이 담기지 않은 토큰을 파싱하면 예외가 발생한다.")
    @Test
    void parseToken_missingRole() {
        // given — 권한 도입 전에 발급된 토큰
        Date now = new Date();
        String token = Jwts.builder()
                .claim(JwtClaim.ID, 1L)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + ACCESS_EXPIRATION))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .compact();

        // when & then
        assertThatThrownBy(() -> jwtProvider.parseToken(token))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("토큰 쌍을 발급하면 액세스/리프레시 토큰이 모두 사용자 id와 권한을 담아 생성된다.")
    @Test
    void createTokenPair() {
        // when
        TokenPair tokenPair = jwtProvider.createTokenPair(new JwtClaim(1L, Role.ADMIN));

        // then
        assertThat(jwtProvider.parseToken(tokenPair.accessToken()).id()).isEqualTo(1L);
        assertThat(jwtProvider.parseToken(tokenPair.accessToken()).role()).isEqualTo(Role.ADMIN);
        assertThat(jwtProvider.parseToken(tokenPair.refreshToken()).id()).isEqualTo(1L);
        assertThat(jwtProvider.parseToken(tokenPair.refreshToken()).role()).isEqualTo(Role.ADMIN);
    }

    @DisplayName("같은 사용자에게 연속으로 발급한 리프레시 토큰은 서로 다르다.")
    @Test
    void createRefreshToken_unique() {
        // when
        String first = jwtProvider.createRefreshToken(new JwtClaim(1L, Role.USER));
        String second = jwtProvider.createRefreshToken(new JwtClaim(1L, Role.USER));

        // then
        assertThat(first).isNotEqualTo(second);
    }

    @DisplayName("만료된 토큰은 만료된 것으로 판별한다.")
    @Test
    void isExpired_expiredToken() {
        // given
        JwtProvider expiredProvider = new JwtProvider(SECRET, -1_000L, -1_000L);
        String expiredToken = expiredProvider.createAccessToken(new JwtClaim(1L, Role.USER));

        // when & then
        assertThat(jwtProvider.isExpired(expiredToken)).isTrue();
    }

    @DisplayName("만료된 토큰을 파싱하면 예외가 발생한다.")
    @Test
    void parseToken_expiredToken() {
        // given
        JwtProvider expiredProvider = new JwtProvider(SECRET, -1_000L, -1_000L);
        String expiredToken = expiredProvider.createAccessToken(new JwtClaim(1L, Role.USER));

        // when & then
        assertThatThrownBy(() -> jwtProvider.parseToken(expiredToken))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_EXPIRED));
    }

    @DisplayName("형식이 잘못된 토큰을 파싱하면 예외가 발생한다.")
    @Test
    void parseToken_invalidToken() {
        // when & then
        assertThatThrownBy(() -> jwtProvider.parseToken("invalid.token.value"))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }

    @DisplayName("다른 비밀키로 서명된 토큰을 파싱하면 예외가 발생한다.")
    @Test
    void parseToken_wrongSignature() {
        // given
        JwtProvider otherProvider =
                new JwtProvider("another-secret-key-for-jwt-provider-9876543210", ACCESS_EXPIRATION, REFRESH_EXPIRATION);
        String token = otherProvider.createAccessToken(new JwtClaim(1L, Role.USER));

        // when & then
        assertThatThrownBy(() -> jwtProvider.parseToken(token))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(AuthErrorCode.AUTH_TOKEN_INVALID));
    }
}
