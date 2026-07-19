package com.neogul.whynago.question.presentation;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.presentation.dto.ChoiceGradingResponse;
import com.neogul.whynago.question.presentation.dto.QuestionResponse;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

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

    @GetMapping("/{questionId}/choices/{choiceId}")
    public ResponseEntity<ChoiceGradingResponse> getChoiceGrading(
            @PathVariable Long questionId,
            @PathVariable Long choiceId
    ) {
        return ResponseEntity.ok(ChoiceGradingResponse.from(questionService.getChoiceGrading(questionId, choiceId)));
    }
}
