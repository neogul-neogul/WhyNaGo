package com.neogul.whynago.learningrecord.implement;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySolvedReader;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RootQuestionReader {

    private final EssaySolvedReader essaySolvedReader;
    private final SolvedMultipleChoiceReader solvedMultipleChoiceReader;
    private final QuestionReader questionReader;

    public Question read(SolvedSession session) {
        Long rootQuestionId = readRootQuestionId(session);
        return questionReader.read(rootQuestionId);
    }

    private Long readRootQuestionId(SolvedSession session) {
        if (session.getType() == QuestionType.ESSAY) {
            return essaySolvedReader.readOrdered(session.getId()).get(0).getQuestionId();
        }
        return solvedMultipleChoiceReader.readOrdered(session.getId()).get(0).getQuestionId();
    }
}
