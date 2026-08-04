package com.neogul.whynago.auth.service;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.implement.JwtProvider;
import com.neogul.whynago.auth.implement.RefreshTokenAppender;
import com.neogul.whynago.auth.implement.RefreshTokenRevoker;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.LogoutCommand;
import com.neogul.whynago.auth.service.dto.ReissueCommand;
import com.neogul.whynago.auth.service.dto.ReissueResult;
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
    private final RefreshTokenAppender refreshTokenAppender;
    private final RefreshTokenRevoker refreshTokenRevoker;

    @Transactional
    public Long signup(SignUpCommand command) {
        userValidator.validateUnique(command.email(), command.nickname());
        String hashedPassword = passwordHasher.hash(command.password());
        User user = userAppender.append(command.email(), hashedPassword, command.nickname());
        return user.getId();
    }

    @Transactional
    public LoginResult login(LoginCommand command) {
        User user = userReader.findByEmail(command.email())
                .orElseThrow(() -> new BusinessException(AuthErrorCode.AUTH_LOGIN_FAILED));
        if (!passwordHasher.matches(command.password(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.AUTH_LOGIN_FAILED);
        }
        TokenPair tokenPair = jwtProvider.createTokenPair(new JwtClaim(user.getId()));
        refreshTokenRevoker.revokeAllByUserId(user.getId());
        refreshTokenAppender.append(user.getId(), tokenPair.refreshToken());
        return new LoginResult(
                tokenPair,
                user.getId(),
                user.getEmail().getValue(),
                user.getNickname(),
                user.getPosition());
    }

    @Transactional
    public ReissueResult reissue(ReissueCommand command) {
        JwtClaim claim = jwtProvider.parseToken(command.refreshToken());
        refreshTokenRevoker.revokeForRotation(command.refreshToken());
        TokenPair tokenPair = jwtProvider.createTokenPair(claim);
        refreshTokenAppender.append(claim.id(), tokenPair.refreshToken());
        return ReissueResult.from(tokenPair);
    }

    // 이미 만료된 access token을 들고 있어도 로그아웃할 수 있어야 하므로 토큰을 검증하지 않고,
    // 이미 폐기된 refresh token이어도 성공으로 처리한다.
    @Transactional
    public void logout(LogoutCommand command) {
        refreshTokenRevoker.revoke(command.refreshToken());
    }
}