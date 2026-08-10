package com.neogul.whynago.interview.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.interview.presentation.dto.AnswerInterviewRequest;
import com.neogul.whynago.interview.presentation.dto.AnswerInterviewResponse;
import com.neogul.whynago.interview.presentation.dto.CompleteInterviewRequest;
import com.neogul.whynago.interview.presentation.dto.CompleteInterviewResponse;
import com.neogul.whynago.interview.presentation.dto.InterviewResultResponse;
import com.neogul.whynago.interview.presentation.dto.StartInterviewResponse;
import com.neogul.whynago.interview.presentation.dto.TodayInterviewResponse;
import com.neogul.whynago.interview.service.InterviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    @GetMapping("/today")
    public ResponseEntity<TodayInterviewResponse> getTodayStatus(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(TodayInterviewResponse.from(interviewService.getTodayStatus(authContext.id())));
    }

    @PostMapping
    public ResponseEntity<StartInterviewResponse> start(@LoginUser AuthContext authContext) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(StartInterviewResponse.from(interviewService.start(authContext.id())));
    }

    @PostMapping("/{interviewId}/answers")
    public ResponseEntity<AnswerInterviewResponse> answer(
            @LoginUser AuthContext authContext,
            @PathVariable Long interviewId,
            @Valid @RequestBody AnswerInterviewRequest request
    ) {
        return ResponseEntity.ok(AnswerInterviewResponse.from(
                interviewService.answer(authContext.id(), interviewId, request.toCommand())
        ));
    }

    @PostMapping("/{interviewId}/complete")
    public ResponseEntity<CompleteInterviewResponse> complete(
            @LoginUser AuthContext authContext,
            @PathVariable Long interviewId,
            @Valid @RequestBody CompleteInterviewRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(CompleteInterviewResponse.from(
                        interviewService.complete(authContext.id(), interviewId, request.toCommand())
                ));
    }

    @DeleteMapping("/{interviewId}")
    public ResponseEntity<Void> cancel(@LoginUser AuthContext authContext, @PathVariable Long interviewId) {
        interviewService.cancel(authContext.id(), interviewId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{interviewId}")
    public ResponseEntity<InterviewResultResponse> findResult(
            @LoginUser AuthContext authContext,
            @PathVariable Long interviewId
    ) {
        return ResponseEntity.ok(InterviewResultResponse.from(
                interviewService.findResult(authContext.id(), interviewId)
        ));
    }
}
