package com.neogul.whynago.solvedsession.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.solvedsession.presentation.dto.SolvedQuestionIdsResponse;
import com.neogul.whynago.solvedsession.service.SolvedQuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solved-questions")
public class SolvedQuestionController {

    private final SolvedQuestionService solvedQuestionService;

    @GetMapping
    public ResponseEntity<SolvedQuestionIdsResponse> findSolvedQuestionIds(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(SolvedQuestionIdsResponse.from(solvedQuestionService.readSolvedQuestionIds(authContext.id())));
    }
}