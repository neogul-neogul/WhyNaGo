package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionTagIdReader {

    private final QuestionTagRepository questionTagRepository;

    public List<Long> readTagIds(Long questionId) {
        return questionTagRepository.findByQuestionIdIn(List.of(questionId)).stream()
                .map(QuestionTag::getTagId)
                .toList();
    }
}
