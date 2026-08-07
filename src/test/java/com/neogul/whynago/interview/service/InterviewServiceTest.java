package com.neogul.whynago.interview.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.interview.domain.DailyInterview;
import com.neogul.whynago.interview.domain.InterviewStatus;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.infra.DailyInterviewRepository;
import com.neogul.whynago.interview.service.dto.AnswerInterviewCommand;
import com.neogul.whynago.interview.service.dto.AnswerInterviewResult;
import com.neogul.whynago.interview.service.dto.CompleteInterviewCommand;
import com.neogul.whynago.interview.service.dto.CompleteInterviewResult;
import com.neogul.whynago.interview.service.dto.InterviewAnswerSnapshotCommand;
import com.neogul.whynago.interview.service.dto.InterviewResultDetail;
import com.neogul.whynago.interview.service.dto.StartInterviewResult;
import com.neogul.whynago.interview.service.dto.TodayInterviewResult;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class InterviewServiceTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DailyInterviewRepository dailyInterviewRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Autowired
    private WrongNoteRepository wrongNoteRepository;

    @MockitoBean
    private EssayAiClient essayAiClient;

    @BeforeEach
    void setUpEssayQuestions() {
        IntStream.range(0, 5).forEach(i -> questionRepository.save(QuestionFixture.essayRoot()));
    }

    @Test
    @DisplayName("면접을 시작하면 진행 중 면접과 대화 식별자가 만들어진다.")
    void start() {
        StartInterviewResult result = interviewService.start(USER_ID);

        assertThat(result.interviewId()).isNotNull();
        assertThat(result.question().id()).isNotNull();
        assertThat(result.totalQuestionCount()).isEqualTo(3);
        assertThat(result.timeLimitSeconds()).isEqualTo(180);

        DailyInterview interview = dailyInterviewRepository.findById(result.interviewId()).orElseThrow();
        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.IN_PROGRESS);
        assertThat(interview.getConversationId()).isNotBlank();
    }

    @Test
    @DisplayName("같은 날 시작한 다른 사용자는 같은 질문을 받는다.")
    void startGivesSameQuestionToEveryone() {
        StartInterviewResult mine = interviewService.start(USER_ID);
        StartInterviewResult others = interviewService.start(11L);

        assertThat(others.question().id()).isEqualTo(mine.question().id());
    }

    @Test
    @DisplayName("오늘 이미 면접을 시작했으면 다시 시작할 수 없다.")
    void startTwiceOnSameDay() {
        interviewService.start(USER_ID);

        assertThatThrownBy(() -> interviewService.start(USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_ALREADY_STARTED_TODAY));
    }

    @Test
    @DisplayName("서술형 문제가 하나도 없으면 면접을 시작할 수 없다.")
    void startWithoutEssayQuestion() {
        questionRepository.deleteAll();

        assertThatThrownBy(() -> interviewService.start(USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_QUESTION_NOT_AVAILABLE));
    }

    @Test
    @DisplayName("오늘 면접을 보지 않았으면 진행 가능 상태를 돌려준다.")
    void getTodayStatusWhenAvailable() {
        TodayInterviewResult result = interviewService.getTodayStatus(USER_ID);

        assertThat(result.status()).isEqualTo("AVAILABLE");
        assertThat(result.interviewId()).isNull();
    }

    @Test
    @DisplayName("면접을 시작했으면 진행 중 상태와 면접 ID를 돌려준다.")
    void getTodayStatusWhenInProgress() {
        StartInterviewResult started = interviewService.start(USER_ID);

        TodayInterviewResult result = interviewService.getTodayStatus(USER_ID);

        assertThat(result.status()).isEqualTo("IN_PROGRESS");
        assertThat(result.interviewId()).isEqualTo(started.interviewId());
    }

    @Test
    @DisplayName("면접을 마쳤으면 완료 상태를 돌려준다.")
    void getTodayStatusWhenCompleted() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        interviewService.complete(USER_ID, interviewId, completeCommand(true, true, true));

        TodayInterviewResult result = interviewService.getTodayStatus(USER_ID);

        assertThat(result.status()).isEqualTo("COMPLETED");
        assertThat(result.interviewId()).isEqualTo(interviewId);
    }

    @Test
    @DisplayName("답변을 제출하면 채점 결과와 다음 꼬리질문을 돌려준다.")
    void answer() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        given(essayAiClient.completedTurns(anyString())).willReturn(0);
        given(essayAiClient.gradeAndGenerateFollowup(anyString(), anyString(), anyString(), anyBoolean()))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 8, "꼬리질문1"));

        AnswerInterviewResult result = interviewService.answer(
                USER_ID, interviewId, new AnswerInterviewCommand("본질문", "답변"));

        assertThat(result.grading().feedback()).isEqualTo("피드백");
        assertThat(result.grading().isCorrect()).isTrue();
        assertThat(result.nextFollowup().question()).isEqualTo("꼬리질문1");
    }

    @Test
    @DisplayName("마지막 문항을 채점하면 꼬리질문이 없다.")
    void answerOnLastTurn() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        given(essayAiClient.completedTurns(anyString())).willReturn(2);
        given(essayAiClient.gradeAndGenerateFollowup(anyString(), anyString(), anyString(), anyBoolean()))
                .willReturn(new GradeAndFollowupResult("피드백", "모범답안", 5, null));

        AnswerInterviewResult result = interviewService.answer(
                USER_ID, interviewId, new AnswerInterviewCommand("꼬리질문2", "답변3"));

        assertThat(result.nextFollowup()).isNull();
        assertThat(result.grading().isCorrect()).isFalse();
    }

    @Test
    @DisplayName("남의 면접에 답변하면 예외가 발생한다.")
    void answerOthersInterview() {
        Long interviewId = interviewService.start(USER_ID).interviewId();

        assertThatThrownBy(() -> interviewService.answer(
                11L, interviewId, new AnswerInterviewCommand("본질문", "답변")))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("없는 면접에 답변하면 예외가 발생한다.")
    void answerMissingInterview() {
        assertThatThrownBy(() -> interviewService.answer(
                USER_ID, 999L, new AnswerInterviewCommand("본질문", "답변")))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND));
    }

    @Test
    @DisplayName("이미 완료된 면접에 답변하면 예외가 발생한다.")
    void answerCompletedInterview() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        interviewService.complete(USER_ID, interviewId, completeCommand(true, true, true));

        assertThatThrownBy(() -> interviewService.answer(
                USER_ID, interviewId, new AnswerInterviewCommand("본질문", "답변")))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_IN_PROGRESS));
    }

    @Test
    @DisplayName("면접을 완료하면 서술형 세션과 문항 3건이 저장되고 오답노트가 만들어진다.")
    void complete() {
        StartInterviewResult started = interviewService.start(USER_ID);

        CompleteInterviewResult result = interviewService.complete(
                USER_ID, started.interviewId(), completeCommand(true, false, true));

        SolvedSession session = solvedSessionRepository.findById(result.solvedSessionId()).orElseThrow();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getTotalCount()).isEqualTo(3);
        assertThat(session.getCorrectCount()).isEqualTo(2);

        List<EssaySolved> items = essaySolvedRepository.findBySolvedSessionIdOrderBySequence(result.solvedSessionId());
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getType()).isEqualTo(ItemType.MAIN);
        assertThat(items.get(0).getQuestionId()).isEqualTo(started.question().id());
        assertThat(items.get(1).getQuestionId()).isNull();
        assertThat(items.get(2).getQuestionId()).isNull();

        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(USER_ID, result.solvedSessionId())).isTrue();

        DailyInterview interview = dailyInterviewRepository.findById(started.interviewId()).orElseThrow();
        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
        assertThat(interview.getSolvedSessionId()).isEqualTo(result.solvedSessionId());
        assertThat(interview.getFocusLossCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("모든 문항을 통과하면 오답노트를 만들지 않는다.")
    void completeAllCorrect() {
        Long interviewId = interviewService.start(USER_ID).interviewId();

        CompleteInterviewResult result = interviewService.complete(
                USER_ID, interviewId, completeCommand(true, true, true));

        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(USER_ID, result.solvedSessionId())).isFalse();
    }

    @Test
    @DisplayName("이미 완료된 면접을 다시 완료하면 예외가 발생한다.")
    void completeTwice() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        interviewService.complete(USER_ID, interviewId, completeCommand(true, true, true));

        assertThatThrownBy(() -> interviewService.complete(USER_ID, interviewId, completeCommand(true, true, true)))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_IN_PROGRESS));
    }

    @Test
    @DisplayName("채점된 턴이 없으면 면접을 취소하고 같은 날 다시 시작할 수 있다.")
    void cancel() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        given(essayAiClient.completedTurns(anyString())).willReturn(0);

        interviewService.cancel(USER_ID, interviewId);

        assertThat(dailyInterviewRepository.findById(interviewId)).isEmpty();
        assertThat(interviewService.start(USER_ID).interviewId()).isNotNull();
    }

    @Test
    @DisplayName("채점된 턴이 있으면 면접을 취소할 수 없다.")
    void cancelAfterGrading() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        given(essayAiClient.completedTurns(anyString())).willReturn(1);

        assertThatThrownBy(() -> interviewService.cancel(USER_ID, interviewId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_CANCELABLE));
    }

    @Test
    @DisplayName("이미 완료된 면접은 취소할 수 없다.")
    void cancelCompletedInterview() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        interviewService.complete(USER_ID, interviewId, completeCommand(true, true, true));
        given(essayAiClient.completedTurns(anyString())).willReturn(0);

        assertThatThrownBy(() -> interviewService.cancel(USER_ID, interviewId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_CANCELABLE));
    }

    @Test
    @DisplayName("완료된 면접의 결과를 문항 순서대로 조회한다.")
    void findResult() {
        StartInterviewResult started = interviewService.start(USER_ID);
        interviewService.complete(USER_ID, started.interviewId(), completeCommand(true, false, true));

        InterviewResultDetail detail = interviewService.findResult(USER_ID, started.interviewId());

        assertThat(detail.status()).isEqualTo("COMPLETED");
        assertThat(detail.totalCount()).isEqualTo(3);
        assertThat(detail.correctCount()).isEqualTo(2);
        assertThat(detail.focusLossCount()).isEqualTo(2);
        assertThat(detail.durationSeconds()).isNotNegative();
        assertThat(detail.items()).hasSize(3);
        assertThat(detail.items().get(0).type()).isEqualTo("MAIN");
        assertThat(detail.items().get(0).questionText()).isEqualTo("본질문");
        assertThat(detail.items().get(2).type()).isEqualTo("FOLLOWUP");
    }

    @Test
    @DisplayName("아직 완료되지 않은 면접의 결과는 조회할 수 없다.")
    void findResultWhenInProgress() {
        Long interviewId = interviewService.start(USER_ID).interviewId();

        assertThatThrownBy(() -> interviewService.findResult(USER_ID, interviewId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_COMPLETED));
    }

    @Test
    @DisplayName("남의 면접 결과는 조회할 수 없다.")
    void findResultOfOthers() {
        Long interviewId = interviewService.start(USER_ID).interviewId();
        interviewService.complete(USER_ID, interviewId, completeCommand(true, true, true));

        assertThatThrownBy(() -> interviewService.findResult(11L, interviewId))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_FOUND));
    }

    private CompleteInterviewCommand completeCommand(boolean first, boolean second, boolean third) {
        return new CompleteInterviewCommand(
                snapshot("본질문", first),
                List.of(snapshot("꼬리질문1", second), snapshot("꼬리질문2", third)),
                2
        );
    }

    private InterviewAnswerSnapshotCommand snapshot(String questionText, boolean isCorrect) {
        return new InterviewAnswerSnapshotCommand(questionText, "답변", "피드백", "모범답안", isCorrect);
    }
}
