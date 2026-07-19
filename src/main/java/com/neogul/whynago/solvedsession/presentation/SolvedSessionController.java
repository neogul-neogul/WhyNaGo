package com.neogul.whynago.solvedsession.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.solvedsession.presentation.dto.CreateSolvedSessionRequest;
import com.neogul.whynago.solvedsession.presentation.dto.CreateSolvedSessionResponse;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solved-sessions")
public class SolvedSessionController {

    private final SolvedSessionService solvedSessionService;

    @PostMapping
    public ResponseEntity<CreateSolvedSessionResponse> create(
            @LoginUser AuthContext authContext,
            @Valid @RequestBody CreateSolvedSessionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateSolvedSessionResponse.from(solvedSessionService.create(authContext.id(), request.toCommand())));
    }
}
