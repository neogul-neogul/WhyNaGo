package com.neogul.whynago.user.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.exception.UserErrorCode;
import com.neogul.whynago.user.implement.dto.UserPage;
import com.neogul.whynago.user.infra.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserReader {

    private final UserRepository userRepository;

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmailValue(email);
    }

    public Optional<User> findBySocial(AuthProvider provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    public User read(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserErrorCode.USER_NOT_FOUND));
    }

    public long countAll() {
        return userRepository.count();
    }

    public long countSignedUpBetween(LocalDateTime from, LocalDateTime to) {
        return userRepository.countByCreatedAtBetween(from, to);
    }

    public UserPage readPage(String keyword, int page, int size) {
        Page<User> users = userRepository.findUsers(normalize(keyword), PageRequest.of(page, size));
        return new UserPage(users.getContent(), users.getTotalElements());
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
