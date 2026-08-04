package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.common.exception.BusinessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    private final SecretKey key;
    private final long accessExpiration;
    private final long refreshExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access.expiration}") long accessExpiration,
            @Value("${jwt.refresh.expiration}") long refreshExpiration) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpiration = accessExpiration;
        this.refreshExpiration = refreshExpiration;
    }

    public TokenPair createTokenPair(JwtClaim claim) {
        return new TokenPair(createAccessToken(claim), createRefreshToken(claim));
    }

    public String createAccessToken(JwtClaim claim) {
        return createToken(claim, accessExpiration);
    }

    public String createRefreshToken(JwtClaim claim) {
        return createToken(claim, refreshExpiration);
    }

    // jti가 없으면 같은 사용자에게 같은 초에 발급한 토큰은 (userId, iat, exp)가 모두 같아
    // 바이트 단위로 동일해진다(iat·exp는 JWT 사양상 초 단위). 그러면 재발급이 이전 토큰을
    // 실제로 교체하지 못하므로, 매번 달라지는 값을 하나 넣어 유일성을 보장한다.
    private String createToken(JwtClaim claim, long expiration) {
        Date now = new Date();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .claim(JwtClaim.ID, claim.id())
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(key)
                .compact();
    }

    public JwtClaim parseToken(String token) {
        Claims claims = getClaims(token);
        Long id = ((Number) claims.get(JwtClaim.ID)).longValue();
        return new JwtClaim(id);
    }

    public boolean isExpired(String token) {
        try {
            getClaims(token);
            return false;
        } catch (BusinessException e) {
            return AuthErrorCode.AUTH_TOKEN_EXPIRED.equals(e.errorCode());
        }
    }

    private Claims getClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_EXPIRED);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_INVALID);
        }
    }
}