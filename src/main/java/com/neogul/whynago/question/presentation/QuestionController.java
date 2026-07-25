package com.neogul.whynago.question.presentation;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.presentation.dto.EssayQuestionResponse;
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

    @GetMapping
    public ResponseEntity<List<QuestionResponse>> findQuestions(
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false, name = "q") String keyword
    ) {
        List<QuestionResponse> responses = questionService.findQuestions(new QuestionSearchCommand(type, difficulty, category, keyword)).stream()
                .map(QuestionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{questionId}/essay")
    public ResponseEntity<EssayQuestionResponse> findEssayQuestion(@PathVariable Long questionId) {
        EssayQuestionResponse response = EssayQuestionResponse.from(questionService.findEssayQuestion(questionId));
        return ResponseEntity.ok(response);
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
