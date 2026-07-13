package com.neogul.whynago.solvedsession.presentation;

import com.neogul.whynago.solvedsession.domain.SessionSource;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.ScoredAnswer;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionCommand;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionResult;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmittedAnswer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/solved-sessions")
public class SolvedSessionController {

    private final SolvedSessionService solvedSessionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubmitSessionResponse submit(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SubmitSessionRequest request
    ) {
        return SubmitSessionResponse.from(solvedSessionService.submit(userId, request.toCommand()));
    }

    public record SubmitSessionRequest(
            @NotNull Long rootQuestionId,
            @NotNull SessionSource source,
            boolean completed,
            @NotEmpty List<@Valid SubmittedAnswerRequest> answers
    ) {

        SubmitSessionCommand toCommand() {
            return new SubmitSessionCommand(
                    rootQuestionId,
                    source,
                    completed,
                    answers.stream()
                            .map(SubmittedAnswerRequest::toCommand)
                            .toList()
            );
        }
    }

    public record SubmittedAnswerRequest(
            @NotNull Long questionId,
            @NotNull Long choiceId
    ) {

        SubmittedAnswer toCommand() {
            return new SubmittedAnswer(questionId, choiceId);
        }
    }

    public record SubmitSessionResponse(
            Long sessionId,
            int totalCount,
            int correctCount,
            int wrongCount,
            double correctRate,
            SessionStatus status,
            List<ScoredAnswerResponse> items
    ) {

        static SubmitSessionResponse from(SubmitSessionResult result) {
            return new SubmitSessionResponse(
                    result.sessionId(),
                    result.totalCount(),
                    result.correctCount(),
                    result.wrongCount(),
                    result.correctRate(),
                    result.status(),
                    result.items().stream()
                            .map(ScoredAnswerResponse::from)
                            .toList()
            );
        }
    }

    public record ScoredAnswerResponse(
            Long questionId,
            Long userChoiceId,
            Long correctChoiceId,
            boolean correct
    ) {

        static ScoredAnswerResponse from(ScoredAnswer scoredAnswer) {
            return new ScoredAnswerResponse(
                    scoredAnswer.questionId(),
                    scoredAnswer.userChoiceId(),
                    scoredAnswer.correctChoiceId(),
                    scoredAnswer.correct()
            );
        }
    }
}
