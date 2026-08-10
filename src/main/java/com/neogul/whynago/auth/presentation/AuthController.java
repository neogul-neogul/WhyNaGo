package com.neogul.whynago.auth.presentation;

import com.neogul.whynago.auth.presentation.dto.GoogleLoginRequest;
import com.neogul.whynago.auth.presentation.dto.LoginRequest;
import com.neogul.whynago.auth.presentation.dto.LoginResponse;
import com.neogul.whynago.auth.presentation.dto.LogoutRequest;
import com.neogul.whynago.auth.presentation.dto.ReissueRequest;
import com.neogul.whynago.auth.presentation.dto.ReissueResponse;
import com.neogul.whynago.auth.presentation.dto.SignUpRequest;
import com.neogul.whynago.auth.presentation.dto.SignUpResponse;
import com.neogul.whynago.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<SignUpResponse> signup(@Valid @RequestBody SignUpRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new SignUpResponse(authService.signup(request.toCommand())));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(LoginResponse.from(authService.login(request.toCommand())));
    }

    @PostMapping("/login/google")
    public ResponseEntity<LoginResponse> googleLogin(@Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(LoginResponse.from(authService.googleLogin(request.toCommand())));
    }

    @PostMapping("/reissue")
    public ResponseEntity<ReissueResponse> reissue(@Valid @RequestBody ReissueRequest request) {
        return ResponseEntity.ok(ReissueResponse.from(authService.reissue(request.toCommand())));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.toCommand());
        return ResponseEntity.noContent().build();
    }
}