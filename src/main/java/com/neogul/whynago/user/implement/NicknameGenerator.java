package com.neogul.whynago.user.implement;

import java.security.SecureRandom;
import org.springframework.stereotype.Component;

@Component
public class NicknameGenerator {

    private static final int RANDOM_BOUND = 1_000_000;

    private final SecureRandom random = new SecureRandom();

    // 닉네임 제약(4~8자)을 항상 만족하도록 길이 7로 고정한다
    public String generate() {
        return "u" + String.format("%06d", random.nextInt(RANDOM_BOUND));
    }
}