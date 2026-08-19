package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.implement.dto.QuestionPage;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.infra.dto.CategoryQuestionCount;
import com.neogul.whynago.question.infra.dto.QuestionTagName;
import java.util.LinkedHashMap;
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
    private final TagReader tagReader;

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
        return questionTagRepository.findTagNames(questionIds).stream()
                .collect(Collectors.groupingBy(
                        QuestionTagName::getQuestionId,
                        LinkedHashMap::new,
                        Collectors.mapping(QuestionTagName::getName, Collectors.toList())
                ));
    }

    // 생성 프롬프트의 네거티브 컨텍스트다. 같은 주제를 다시 만들지 않게 기존 서술형 제목을 넘긴다.
    public List<String> readEssayTitles(Category category, List<String> tagNames) {
        return questionRepository.findEssayTitles(category, tagNames.isEmpty() ? List.of("") : tagNames);
    }

    // 생성 실패·쿼터 소진 시 쓰는 폴백 후보다. 객관식·서술형을 모두 후보로 삼는다.
    public List<Question> readApprovedByCategories(List<Category> categories, int limit) {
        if (categories.isEmpty() || limit <= 0) {
            return List.of();
        }
        return questionRepository.findApprovedByCategories(categories, PageRequest.of(0, limit));
    }

    public List<Question> readApprovedByDifficulty(Difficulty difficulty) {
        return questionRepository.findApprovedByDifficulty(difficulty);
    }

    public List<String> readTagDictionary(Category category) {
        return tagReader.readNamesByCategory(category);
    }

    private String normalize(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return null;
        }
        return keyword.trim();
    }
}
