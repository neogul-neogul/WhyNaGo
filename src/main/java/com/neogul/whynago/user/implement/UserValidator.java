package com.neogul.whynago.user.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserValidator {

    private final UserRepository userRepository;

    public void validateUnique(String email, String nickname) {
        if (userRepository.existsByEmailValue(email)) {
            throw new BusinessException(UserErrorCode.USER_DUPLICATE_EMAIL);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(UserErrorCode.USER_DUPLICATE_NICKNAME);
        }
    }
}
