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
import com.neogul.whynago.interview.service.dto.StartInterviewResult;
import com.neogul.whynago.interview.service.dto.TodayInterviewResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.ConversationIdGenerator;
import com.neogul.whynago.question.implement.EssayAnswerEvaluator;
import com.neogul.whynago.question.implement.dto.EssayEvaluation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewService {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int TOTAL_QUESTION_COUNT = 3;
    private static final int TIME_LIMIT_SECONDS = 180;

    private final DailyQuestionResolver dailyQuestionResolver;
    private final DailyInterviewAppender dailyInterviewAppender;
    private final DailyInterviewReader dailyInterviewReader;
    private final InterviewCanceller interviewCanceller;
    private final InterviewRecordRegistrar interviewRecordRegistrar;
    private final InterviewResultAssembler interviewResultAssembler;
    private final ConversationIdGenerator conversationIdGenerator;
    private final EssayAnswerEvaluator essayAnswerEvaluator;

    @Transactional(readOnly = true)
    public TodayInterviewResult getTodayStatus(Long userId) {
        return TodayInterviewResult.from(dailyInterviewReader.readByDate(userId, LocalDate.now(KST)));
    }

    @Transactional
    public StartInterviewResult start(Long userId) {
        LocalDate today = LocalDate.now(KST);
        Question question = dailyQuestionResolver.resolve(today);
        DailyInterview interview = dailyInterviewAppender.append(
                userId,
                today,
                question,
                conversationIdGenerator.generate(),
                LocalDateTime.now()
        );

        return StartInterviewResult.of(interview, question, TOTAL_QUESTION_COUNT, TIME_LIMIT_SECONDS);
    }

    @Transactional(readOnly = true)
    public AnswerInterviewResult answer(Long userId, Long interviewId, AnswerInterviewCommand command) {
        DailyInterview interview = dailyInterviewReader.readInProgress(interviewId, userId);
        EssayEvaluation evaluation = essayAnswerEvaluator.evaluate(
                interview.getConversationId(),
                command.question(),
                command.answer()
        );

        return AnswerInterviewResult.from(evaluation);
    }

    @Transactional
    public CompleteInterviewResult complete(Long userId, Long interviewId, CompleteInterviewCommand command) {
        DailyInterview interview = dailyInterviewReader.readInProgress(interviewId, userId);
        LocalDateTime completedAt = LocalDateTime.now();

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
}
