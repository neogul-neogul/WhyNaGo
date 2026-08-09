package com.neogul.whynago.user.implement;

import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.infra.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserAppender {

    private final UserRepository userRepository;

    public User append(String email, String hashedPassword, String nickname) {
        User user = User.create(email, hashedPassword, nickname);
        return userRepository.save(user);
    }

    public User appendSocial(String email, String nickname, AuthProvider provider, String providerId) {
        User user = User.createSocial(email, nickname, provider, providerId);
        return userRepository.save(user);
    }
}