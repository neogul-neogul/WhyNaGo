package com.neogul.whynago.question.service;

import com.neogul.whynago.question.implement.EssayAnswerEvaluator;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EssayAnswerService {

    private final QuestionReader questionReader;
    private final EssayAnswerEvaluator essayAnswerEvaluator;

    public EssayAnswerResult evaluate(Long questionId, EvaluateEssayAnswerCommand command) {
        questionReader.readEssayQuestion(questionId);
        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(command.thread());
        return EssayAnswerResult.from(evaluation);
    }
}
