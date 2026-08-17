package com.neogul.whynago.auth.service;

import com.neogul.whynago.auth.domain.JwtClaim;
import com.neogul.whynago.auth.domain.TokenPair;
import com.neogul.whynago.auth.exception.AuthErrorCode;
import com.neogul.whynago.auth.implement.GoogleTokenVerifier;
import com.neogul.whynago.auth.implement.JwtProvider;
import com.neogul.whynago.auth.implement.RefreshTokenAppender;
import com.neogul.whynago.auth.implement.RefreshTokenRevoker;
import com.neogul.whynago.auth.implement.RefreshTokenRotator;
import com.neogul.whynago.auth.implement.dto.GoogleAccount;
import com.neogul.whynago.auth.service.dto.GoogleLoginCommand;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.LogoutCommand;
import com.neogul.whynago.auth.service.dto.ReissueCommand;
import com.neogul.whynago.auth.service.dto.ReissueResult;
import com.neogul.whynago.auth.service.dto.SignUpCommand;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.notification.implement.NotificationSettingAppender;
import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.implement.NicknameGenerator;
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
    private final NicknameGenerator nicknameGenerator;
    private final GoogleTokenVerifier googleTokenVerifier;
    private final JwtProvider jwtProvider;
    private final RefreshTokenAppender refreshTokenAppender;
    private final RefreshTokenRevoker refreshTokenRevoker;
    private final RefreshTokenRotator refreshTokenRotator;
    private final NotificationSettingAppender notificationSettingAppender;

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
        if (!user.isLocal()) {
            throw new BusinessException(AuthErrorCode.AUTH_SOCIAL_ACCOUNT);
        }
        if (!passwordHasher.matches(command.password(), user.getPassword())) {
            throw new BusinessException(AuthErrorCode.AUTH_LOGIN_FAILED);
        }
        return issueLogin(user);
    }

    @Transactional
    public LoginResult googleLogin(GoogleLoginCommand command) {
        GoogleAccount account = googleTokenVerifier.verify(command.credential());
        User user = userReader.findBySocial(AuthProvider.GOOGLE, account.sub())
                .orElseGet(() -> registerGoogleUser(account));
        return issueLogin(user);
    }

    // 권한 변경이 늦게 반영되지 않도록 role은 토큰이 아니라 DB에서 다시 읽는다
    @Transactional
    public ReissueResult reissue(ReissueCommand command) {
        JwtClaim claim = jwtProvider.parseToken(command.refreshToken());
        refreshTokenRotator.rotate(command.refreshToken());
        User user = userReader.read(claim.id());
        TokenPair tokenPair = jwtProvider.createTokenPair(new JwtClaim(user.getId(), user.getRole()));
        refreshTokenAppender.append(user.getId(), tokenPair.refreshToken());
        return ReissueResult.from(tokenPair);
    }

    @Transactional
    public void logout(LogoutCommand command) {
        refreshTokenRevoker.revoke(command.refreshToken());
    }

    // 이메일이 겹치는 기존 계정과는 연동하지 않고 안내한다 — 계정 하나에 로그인 수단 하나
    private User registerGoogleUser(GoogleAccount account) {
        if (userReader.findByEmail(account.email()).isPresent()) {
            throw new BusinessException(AuthErrorCode.AUTH_LOCAL_ACCOUNT);
        }
        return userAppender.appendSocial(
                account.email(), nicknameGenerator.generate(), AuthProvider.GOOGLE, account.sub());
    }

    // 로그인 시 기존 refresh token을 전부 폐기하고 새로 발급한다 (1계정 1세션)
    private LoginResult issueLogin(User user) {
        // 알림 설정 화면을 한 번도 열지 않은 사용자도 리마인더를 받도록 로그인 시점에 기본 설정을 보장한다
        notificationSettingAppender.appendDefaultIfAbsent(user.getId());
        TokenPair tokenPair = jwtProvider.createTokenPair(new JwtClaim(user.getId(), user.getRole()));
        refreshTokenRevoker.revokeAllByUserId(user.getId());
        refreshTokenAppender.append(user.getId(), tokenPair.refreshToken());
        return new LoginResult(
                tokenPair,
                user.getId(),
                user.getEmail().getValue(),
                user.getNickname(),
                user.getPosition(),
                user.getRole());
    }
}