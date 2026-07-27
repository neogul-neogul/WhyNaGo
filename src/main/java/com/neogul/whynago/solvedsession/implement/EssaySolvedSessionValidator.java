package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.question.implement.QuestionReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssaySolvedSessionValidator {

    private final QuestionReader questionReader;

    public void validate(Long rootQuestionId) {
        questionReader.readEssayQuestion(rootQuestionId);
    }
}
