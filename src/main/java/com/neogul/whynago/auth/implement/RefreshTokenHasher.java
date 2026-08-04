package com.neogul.whynago.auth.implement;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {

    private static final String ALGORITHM = "SHA-256";

    /**
     * 저장된 리프레시 토큰을 조회 키로 찾아야 하므로 salt 없는 결정적 해시를 사용한다.
     * 토큰 자체가 충분히 긴 서명값이라 무차별 대입 위험이 없어, 비밀번호와 달리 bcrypt가 필요하지 않다.
     */
    public String hash(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("지원하지 않는 해시 알고리즘입니다: " + ALGORITHM, e);
        }
    }
}