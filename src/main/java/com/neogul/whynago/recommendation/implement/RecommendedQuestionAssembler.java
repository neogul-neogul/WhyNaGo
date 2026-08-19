package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.recommendation.service.dto.RecommendedQuestionResult;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 문항 목록에 태그를 붙여 응답 모델로 바꾼다. 태그는 문항마다 한 번씩 조회하지 않고 한 번에 읽는다.
@Component
@RequiredArgsConstructor
public class RecommendedQuestionAssembler {

    private final QuestionReader questionReader;

    public List<RecommendedQuestionResult> assemble(List<Question> questions) {
        if (questions.isEmpty()) {
            return List.of();
        }
        Map<Long, List<String>> tagNames = questionReader.readTagNames(
                questions.stream().map(Question::getId).toList());

        return questions.stream()
                .map(question -> RecommendedQuestionResult.of(
                        question,
                        tagNames.getOrDefault(question.getId(), List.of())))
                .toList();
    }
}
