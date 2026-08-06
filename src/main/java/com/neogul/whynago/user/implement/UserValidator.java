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

    // 본인의 기존 닉네임과는 중복으로 보지 않는다
    public void validateNicknameUniqueForUpdate(Long userId, String nickname) {
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new BusinessException(UserErrorCode.USER_DUPLICATE_NICKNAME);
        }
    }
}
