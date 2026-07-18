package com.neogul.whynago.auth.presentation;

import com.neogul.whynago.auth.presentation.dto.LoginRequest;
import com.neogul.whynago.auth.presentation.dto.LoginResponse;
import com.neogul.whynago.auth.presentation.dto.SignUpRequest;
import com.neogul.whynago.auth.presentation.dto.SignUpResponse;
import com.neogul.whynago.auth.service.AuthService;
import com.neogul.whynago.auth.service.dto.LoginCommand;
import com.neogul.whynago.auth.service.dto.LoginResult;
import com.neogul.whynago.auth.service.dto.SignUpCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public SignUpResponse signup(@Valid @RequestBody SignUpRequest request) {
        Long userId = authService.signup(
                new SignUpCommand(request.email(), request.password(), request.nickname()));
        return new SignUpResponse(userId);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(new LoginCommand(request.email(), request.password()));
        return new LoginResponse(
                result.tokenPair().accessToken(),
                result.tokenPair().refreshToken(),
                result.userId(),
                result.email(),
                result.nickname(),
                result.position());
    }
}