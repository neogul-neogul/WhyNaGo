package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionAppender;
import com.neogul.whynago.recommendation.domain.GeneratedEssay;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 검증을 통과한 문항만 저장한다. 저장하지 않으면 통계가 쌓이지 않고 풀이 이력이 문항을 참조할 수 없다.
@Component
@RequiredArgsConstructor
public class GeneratedQuestionAppender {

    private final QuestionAppender questionAppender;

    public Question append(GeneratedEssay generated) {
        Question question = Question.generated(
                generated.title(),
                generated.content(),
                generated.difficulty(),
                generated.category(),
                generated.modelAnswer(),
                generated.gradingCriteria()
        );
        return questionAppender.append(question, generated.tags());
    }
}
