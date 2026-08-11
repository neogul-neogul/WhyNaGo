package com.neogul.whynago.question.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.presentation.dto.ChoiceGradingResponse;
import com.neogul.whynago.question.presentation.dto.EssayQuestionResponse;
import com.neogul.whynago.question.presentation.dto.EssaySessionResponse;
import com.neogul.whynago.question.presentation.dto.EvaluateEssayAnswerRequest;
import com.neogul.whynago.question.presentation.dto.EvaluateEssayAnswerResponse;
import com.neogul.whynago.question.presentation.dto.QuestionResponse;
import com.neogul.whynago.question.service.EssayAnswerService;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;
    private final EssayAnswerService essayAnswerService;

    // 인증이 선택인 경로라 비로그인 요청은 authContext가 null이다.
    @GetMapping
    public ResponseEntity<List<QuestionResponse>> findQuestions(
            @LoginUser AuthContext authContext,
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false, name = "q") String keyword
    ) {
        Long userId = authContext == null ? null : authContext.id();
        QuestionSearchCommand command = new QuestionSearchCommand(type, difficulty, category, keyword);
        List<QuestionResponse> responses = questionService.findQuestions(userId, command).stream()
                .map(QuestionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{questionId}/choices/{choiceId}")
    public ResponseEntity<ChoiceGradingResponse> getChoiceGrading(
            @PathVariable Long questionId,
            @PathVariable Long choiceId
    ) {
        return ResponseEntity.ok(ChoiceGradingResponse.from(questionService.getChoiceGrading(questionId, choiceId)));
    }

    @GetMapping("/{questionId}/essay")
    public ResponseEntity<EssayQuestionResponse> findEssayQuestion(@PathVariable Long questionId) {
        EssayQuestionResponse response = EssayQuestionResponse.from(questionService.findEssayQuestion(questionId));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{questionId}/essay/sessions")
    public ResponseEntity<EssaySessionResponse> startEssaySession(@PathVariable Long questionId) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(EssaySessionResponse.from(essayAnswerService.startSession(questionId)));
    }

    @PostMapping("/{questionId}/essay/answers")
    public ResponseEntity<EvaluateEssayAnswerResponse> evaluateEssayAnswer(
            @PathVariable Long questionId,
            @Valid @RequestBody EvaluateEssayAnswerRequest request
    ) {
        EssayAnswerResult result = essayAnswerService.evaluate(questionId, request.toCommand());
        return ResponseEntity.ok(EvaluateEssayAnswerResponse.from(result));
    }
}
