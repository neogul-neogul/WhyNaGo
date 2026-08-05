package com.neogul.whynago.auth.implement;

import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.common.exception.BusinessException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RefreshTokenHasher {

    private static final String ALGORITHM = "SHA-256";

    public String hash(String refreshToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return HexFormat.of().formatHex(digest.digest(refreshToken.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            log.error("지원하지 않는 해시 알고리즘 - algorithm={}", ALGORITHM, e);
            throw new BusinessException(AuthErrorCode.AUTH_TOKEN_HASH_FAILED);
        }
    }
}