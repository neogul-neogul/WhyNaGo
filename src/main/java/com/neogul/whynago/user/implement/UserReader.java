package com.neogul.whynago.user.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.infra.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailValue(email);
    }

    public User read(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }
}
