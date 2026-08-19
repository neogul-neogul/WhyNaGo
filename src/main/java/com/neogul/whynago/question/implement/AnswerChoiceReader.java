package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AnswerChoiceReader {

    private final AnswerChoiceRepository answerChoiceRepository;

    public AnswerChoice read(Long choiceId) {
        return answerChoiceRepository.findById(choiceId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.CHOICE_NOT_FOUND));
    }

    public AnswerChoice readCorrectChoice(Long questionId) {
        return answerChoiceRepository.findFirstByQuestionIdAndIsCorrectTrue(questionId)
                .orElseThrow(() -> new BusinessException(QuestionErrorCode.CHOICE_NOT_FOUND));
    }

    public List<AnswerChoice> readChoices(Long questionId) {
        return answerChoiceRepository.findByQuestionIdOrderBySequence(questionId);
    }

    // 오개념 카탈로그로 쓰는 오답 해설이다. 태그로 좁힐 수 있으면 태그로, 없으면 카테고리로 뽑는다.
    public List<String> readWrongExplanations(Category category, List<String> tagNames, int limit) {
        PageRequest page = PageRequest.of(0, limit);
        if (tagNames.isEmpty()) {
            return answerChoiceRepository.findWrongExplanationsByCategory(category, page);
        }
        List<String> byTag = answerChoiceRepository.findWrongExplanationsByTagNames(tagNames, page);
        if (!byTag.isEmpty()) {
            return byTag;
        }
        // 태그로 뽑히는 오답 해설이 없으면 카테고리 전체로 넓힌다. 프롬프트가 비면 생성 품질이 떨어진다.
        return answerChoiceRepository.findWrongExplanationsByCategory(category, page);
    }
}
