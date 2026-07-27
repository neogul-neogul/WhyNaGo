package com.neogul.whynago.solvedsession.presentation;

import com.neogul.whynago.solvedsession.presentation.dto.CreateEssaySolvedSessionRequest;
import com.neogul.whynago.solvedsession.presentation.dto.CreateEssaySolvedSessionResponse;
import com.neogul.whynago.solvedsession.service.EssaySolvedSessionService;
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
@RequestMapping("/api/solved-sessions/essay")
public class EssaySolvedSessionController {

    private final EssaySolvedSessionService essaySolvedSessionService;

    @PostMapping
    public ResponseEntity<CreateEssaySolvedSessionResponse> create(
            Long userId,
            @Valid @RequestBody CreateEssaySolvedSessionRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CreateEssaySolvedSessionResponse.from(essaySolvedSessionService.create(userId, request.toCommand())));
    }
}
