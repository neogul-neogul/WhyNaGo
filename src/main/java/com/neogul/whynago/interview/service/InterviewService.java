package com.neogul.whynago.interview.service;

import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.implement.DailyInterviewAppender;
import com.neogul.whynago.interview.implement.DailyInterviewReader;
import com.neogul.whynago.interview.implement.DailyQuestionResolver;
import com.neogul.whynago.interview.implement.InterviewCanceller;
import com.neogul.whynago.interview.implement.InterviewRecordRegistrar;
import com.neogul.whynago.interview.implement.InterviewResultAssembler;
import com.neogul.whynago.interview.service.dto.AnswerInterviewCommand;
import com.neogul.whynago.interview.service.dto.AnswerInterviewResult;
import com.neogul.whynago.interview.service.dto.CompleteInterviewCommand;
import com.neogul.whynago.interview.service.dto.CompleteInterviewResult;
import com.neogul.whynago.interview.service.dto.InterviewResultDetail;
import com.neogul.whynago.interview.service.dto.InterviewSummaryResult;
import com.neogul.whynago.interview.service.dto.StartInterviewResult;
import com.neogul.whynago.interview.service.dto.TodayInterviewResult;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.ConversationIdGenerator;
import com.neogul.whynago.question.implement.EssayAnswerEvaluator;
import com.neogul.whynago.question.implement.EssayMasteryRecorder;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.implement.SolvingTimeReader;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final int TOTAL_QUESTION_COUNT = 3;
    private static final int TIME_LIMIT_SECONDS = 180;

    private final DailyQuestionResolver dailyQuestionResolver;
    private final DailyInterviewAppender dailyInterviewAppender;
    private final DailyInterviewReader dailyInterviewReader;
    private final InterviewCanceller interviewCanceller;
    private final InterviewRecordRegistrar interviewRecordRegistrar;
    private final InterviewResultAssembler interviewResultAssembler;
    private final ConversationIdGenerator conversationIdGenerator;
    private final SolvingTimeReader solvingTimeReader;
    private final EssayAnswerEvaluator essayAnswerEvaluator;
    private final EssayMasteryRecorder essayMasteryRecorder;
    private final QuestionReader questionReader;
    // 날짜(오늘의 질문 핀)와 시각(면접 시작·완료)을 같은 시계에서 얻어야 한다. 날짜만 KST로 고정하고
    // 시각은 LocalDateTime.now()로 두면 UTC 컨테이너에서 둘이 다른 날을 가리킨다.
    private final Clock clock;

    @Transactional(readOnly = true)
    public TodayInterviewResult getTodayStatus(Long userId) {
        return TodayInterviewResult.from(dailyInterviewReader.readByDate(userId, LocalDate.now(clock)));
    }

    @Transactional
    public StartInterviewResult start(Long userId) {
        LocalDate today = LocalDate.now(clock);
        Question question = dailyQuestionResolver.resolve(today);
        DailyInterview interview = dailyInterviewAppender.append(
                userId,
                today,
                question,
                conversationIdGenerator.generate(),
                LocalDateTime.now(clock)
        );

        return StartInterviewResult.of(interview, question, TOTAL_QUESTION_COUNT, TIME_LIMIT_SECONDS);
    }

    // 트랜잭션을 걸지 않는다. AI 채점이 수 초 걸리는 동안 커넥션을 붙잡지 않기 위해서다.
    // 읽기 전용 트랜잭션으로 두면 안쪽 숙련도 기록이 그 트랜잭션에 합류해 쓰기가 flush되지 않는다.
    public AnswerInterviewResult answer(Long userId, Long interviewId, AnswerInterviewCommand command) {
        DailyInterview interview = dailyInterviewReader.readInProgress(interviewId, userId);
        // 채점 전에 문항을 읽는다. 루브릭을 프롬프트에 내려야 하고, 같은 인스턴스를 숙련도 기록에 재사용한다.
        Question question = questionReader.read(interview.getQuestionId());
        EssayGradingTarget target = new EssayGradingTarget(
                command.question(),
                command.answer(),
                question.getRubric(),
                solvingTimeReader.read(question.getId(), command.elapsedSeconds())
        );
        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                interview.getConversationId(), target, EssayGradingMode.INTERVIEW);
        essayMasteryRecorder.record(userId, question, evaluation);

        return AnswerInterviewResult.from(evaluation);
    }

    @Transactional
    public CompleteInterviewResult complete(Long userId, Long interviewId, CompleteInterviewCommand command) {
        DailyInterview interview = dailyInterviewReader.readInProgress(interviewId, userId);
        LocalDateTime completedAt = LocalDateTime.now(clock);

        Long solvedSessionId = interviewRecordRegistrar.register(
                userId,
                interview.getQuestionId(),
                command.toPayloads(),
                interview.getStartedAt(),
                completedAt
        );
        interview.complete(solvedSessionId, command.focusLossCount(), completedAt);

        return new CompleteInterviewResult(interview.getId(), solvedSessionId);
    }

    @Transactional
    public void cancel(Long userId, Long interviewId) {
        interviewCanceller.cancel(dailyInterviewReader.readOwned(interviewId, userId));
    }

    @Transactional(readOnly = true)
    public InterviewResultDetail findResult(Long userId, Long interviewId) {
        DailyInterview interview = dailyInterviewReader.readCompleted(interviewId, userId);
        return InterviewResultDetail.from(interviewResultAssembler.assemble(interview));
    }

    @Transactional(readOnly = true)
    public List<InterviewSummaryResult> findAll(Long userId) {
        return dailyInterviewReader.readCompletedAll(userId).stream()
                .map(interviewResultAssembler::assembleSummary)
                .map(InterviewSummaryResult::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public int countCompleted(Long userId) {
        return dailyInterviewReader.readCompletedAll(userId).size();
    }
}
