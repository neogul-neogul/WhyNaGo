package com.neogul.whynago.auth.service;

import com.neogul.whynago.auth.service.dto.SignUpCommand;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.implement.PasswordHasher;
import com.neogul.whynago.user.implement.UserAppender;
import com.neogul.whynago.user.implement.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserValidator userValidator;
    private final PasswordHasher passwordHasher;
    private final UserAppender userAppender;

    @Transactional
    public Long signup(SignUpCommand command) {
        userValidator.validateUnique(command.email(), command.nickname());
        String hashedPassword = passwordHasher.hash(command.password());
        User user = userAppender.append(command.email(), hashedPassword, command.nickname());
        return user.getId();
    }
}