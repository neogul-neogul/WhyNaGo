package com.neogul.whynago.question.service;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.ConversationIdGenerator;
import com.neogul.whynago.question.implement.EssayAnswerEvaluator;
import com.neogul.whynago.question.implement.EssayMasteryRecorder;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.implement.SolvingTimeReader;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import com.neogul.whynago.question.service.dto.EssayAnswerResult;
import com.neogul.whynago.question.service.dto.EssaySessionResult;
import com.neogul.whynago.question.service.dto.EvaluateEssayAnswerCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EssayAnswerService {

    private final QuestionReader questionReader;
    private final ConversationIdGenerator conversationIdGenerator;
    private final SolvingTimeReader solvingTimeReader;
    private final EssayAnswerEvaluator essayAnswerEvaluator;
    private final EssayMasteryRecorder essayMasteryRecorder;

    public EssaySessionResult startSession(Long questionId) {
        questionReader.readEssayQuestion(questionId);
        return new EssaySessionResult(conversationIdGenerator.generate());
    }

    // 숙련도를 여기서 기록한다. 클라이언트가 채점 결과를 되돌려주는 저장 경로에 맡기면
    // 값이 조작될 수 있고, 세션을 중도에 이탈한 답변의 판정은 아예 남지 않는다.
    //
    // 트랜잭션을 이 메서드에 걸지 않는다. AI 채점은 수 초가 걸리므로 그 시간 동안 DB 커넥션을
    // 붙잡게 된다. 기록은 MasteryService가 자기 트랜잭션에서 처리한다.
    public EssayAnswerResult evaluate(Long userId, Long questionId, EvaluateEssayAnswerCommand command) {
        Question question = questionReader.readEssayQuestion(questionId);
        EssayGradingTarget target = new EssayGradingTarget(
                command.question(),
                command.answer(),
                question.getRubric(),
                solvingTimeReader.read(questionId, command.elapsedSeconds())
        );
        EssayEvaluation evaluation =
                essayAnswerEvaluator.evaluate(command.conversationId(), target, EssayGradingMode.PRACTICE);
        essayMasteryRecorder.record(userId, question, evaluation);

        return EssayAnswerResult.from(evaluation);
    }
}
