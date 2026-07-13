package com.neogul.whynago.question.presentation;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.question.service.QuestionService.QuestionResult;
import com.neogul.whynago.question.service.QuestionService.QuestionSearchCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/questions")
public class QuestionController {

    private final QuestionService questionService;

    @GetMapping
    public List<QuestionResponse> findQuestions(
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false, name = "q") String keyword
    ) {
        return questionService.findQuestions(new QuestionSearchCommand(type, difficulty, category, keyword)).stream()
                .map(QuestionResponse::from)
                .toList();
    }

    public record QuestionResponse(
            Long id,
            String title,
            String content,
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String explanation,
            boolean root,
            List<ChoiceResponse> choices,
            List<String> tags
    ) {

        static QuestionResponse from(QuestionResult result) {
            return new QuestionResponse(
                    result.id(),
                    result.title(),
                    result.content(),
                    result.type(),
                    result.difficulty(),
                    result.category(),
                    result.explanation(),
                    result.root(),
                    result.choices().stream()
                            .map(ChoiceResponse::from)
                            .toList(),
                    result.tags()
            );
        }
    }

    public record ChoiceResponse(
            Long id,
            String content,
            int sequence,
            String explanation,
            Long relatedQuestionId
    ) {

        static ChoiceResponse from(QuestionService.ChoiceResult result) {
            return new ChoiceResponse(
                    result.id(),
                    result.content(),
                    result.sequence(),
                    result.explanation(),
                    result.relatedQuestionId()
            );
        }
    }
}
