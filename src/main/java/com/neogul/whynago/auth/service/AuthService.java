package com.neogul.whynago.auth.service;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.implement.JwtProvider;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.SignUpCommand;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.implement.PasswordHasher;
import com.neogul.whynago.user.implement.UserAppender;
import com.neogul.whynago.user.implement.UserReader;
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
    private final UserReader userReader;
    private final JwtProvider jwtProvider;

    @Transactional
    public Long signup(SignUpCommand command) {
        userValidator.validateUnique(command.email(), command.nickname());
        String hashedPassword = passwordHasher.hash(command.password());
        User user = userAppender.append(command.email(), hashedPassword, command.nickname());
        return user.getId();
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginCommand command) {
        User user = userReader.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.AUTH_LOGIN_FAILED));
        if (!passwordHasher.matches(command.password(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.AUTH_LOGIN_FAILED);
        }
        TokenPair tokenPair = jwtProvider.createTokenPair(new JwtClaim(user.getId()));
        return new LoginResult(
                tokenPair,
                user.getId(),
                user.getEmail().getValue(),
                user.getNickname(),
                user.getPosition());
    }
}