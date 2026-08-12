package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.dto.QuestionPage;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.infra.dto.CategoryQuestionCount;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionReader {

    private final QuestionRepository questionRepository;
    private final QuestionTagRepository questionTagRepository;

    public Question read(Long questionId) {
        return questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    public List<Question> readQuestions(
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String keyword
    ) {
        return questionRepository.findQuestions(type, difficulty, category, normalize(keyword), Pageable.unpaged())
                .getContent();
    }

    public QuestionPage readQuestionPage(
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String keyword,
            int page,
            int size
    ) {
        Page<Question> questions = questionRepository.findQuestions(
                type,
                difficulty,
                category,
                normalize(keyword),
                PageRequest.of(page, size)
        );
        return new QuestionPage(questions.getContent(), questions.getTotalElements());
    }

    public Question readEssayQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.QUESTION_NOT_FOUND));
        if (!question.isEssay()) {
            throw new BusinessException(QuestionErrorCode.QUESTION_NOT_ESSAY);
        }
        return question;
    }

    public Map<Category, Integer> countByCategory() {
        return questionRepository.countGroupByCategory().stream()
                .collect(Collectors.toMap(
                        CategoryQuestionCount::getCategory,
                        count -> (int) count.getTotal()
                ));
    }

    public List<Question> readAll(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return List.of();
        }
        return questionRepository.findAllById(questionIds);
    }

    public Map<Long, List<String>> readTagNames(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionTagRepository.findByQuestionIdIn(questionIds).stream()
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(QuestionTag::getName, Collectors.toList())
                ));
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
