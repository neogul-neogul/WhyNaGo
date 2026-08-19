package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import com.neogul.whynago.fixture.GradeAndFollowupResultFixture;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.service.InterviewService;
import com.neogul.whynago.interview.service.dto.AnswerInterviewCommand;
import com.neogul.whynago.interview.service.dto.AnswerInterviewResult;
import com.neogul.whynago.interview.service.dto.CompleteInterviewCommand;
import com.neogul.whynago.interview.service.dto.CompleteInterviewResult;
import com.neogul.whynago.interview.service.dto.InterviewAnswerSnapshotCommand;
import com.neogul.whynago.interview.service.dto.InterviewResultDetail;
import com.neogul.whynago.interview.service.dto.StartInterviewResult;
import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class DailyInterviewIntegrationTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;

    @Autowired
    private InterviewService interviewService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private WrongNoteRepository wrongNoteRepository;

    @MockitoBean
    private EssayAiClient essayAiClient;

    @BeforeEach
    void setUpEssayQuestion() {
        questionRepository.save(QuestionFixture.essayRoot());
    }

    @Test
    @DisplayName("면접을 시작해 3문항을 답하고 완료하면 결과를 조회할 수 있다.")
    void runFullInterview() {
        given(essayAiClient.completedTurns(anyString())).willReturn(0, 1, 2);
        given(essayAiClient.gradeAndGenerateFollowup(
                anyString(), anyString(), anyString(), anyBoolean(), any(EssayGradingMode.class)))
                .willReturn(
                        GradeAndFollowupResultFixture.of("피드백1", "모범답안1", 9, "꼬리질문1"),
                        GradeAndFollowupResultFixture.of("피드백2", "모범답안2", 4, "꼬리질문2"),
                        GradeAndFollowupResultFixture.of("피드백3", "모범답안3", 8, null)
                );

        StartInterviewResult started = interviewService.start(USER_ID);

        List<InterviewAnswerSnapshotCommand> snapshots = new ArrayList<>();
        String currentQuestion = started.question().content();
        for (int turn = 1; turn <= 3; turn++) {
            AnswerInterviewResult answered = interviewService.answer(
                    USER_ID, started.interviewId(), new AnswerInterviewCommand(currentQuestion, "답변" + turn));
            snapshots.add(new InterviewAnswerSnapshotCommand(
                    currentQuestion,
                    "답변" + turn,
                    answered.grading().feedback(),
                    answered.grading().modelAnswer(),
                    answered.grading().isCorrect(),
                    answered.grading().score(),
                    null
            ));
            if (turn == 3) {
                assertThat(answered.nextFollowup()).isNull();
                break;
            }
            currentQuestion = answered.nextFollowup().question();
        }

        CompleteInterviewResult completed = interviewService.complete(
                USER_ID,
                started.interviewId(),
                new CompleteInterviewCommand(snapshots.get(0), snapshots.subList(1, 3), 1)
        );

        InterviewResultDetail detail = interviewService.findResult(USER_ID, started.interviewId());
        assertThat(detail.totalCount()).isEqualTo(3);
        assertThat(detail.correctCount()).isEqualTo(2);
        assertThat(detail.focusLossCount()).isEqualTo(1);
        assertThat(detail.items()).extracting(item -> item.questionText())
                .containsExactly(started.question().content(), "꼬리질문1", "꼬리질문2");
        assertThat(detail.items()).extracting(item -> item.isCorrect())
                .containsExactly(true, false, true);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(USER_ID, completed.solvedSessionId())).isTrue();
    }

    @Test
    @DisplayName("면접을 마친 뒤에는 같은 날 다시 시작할 수 없다.")
    void cannotRestartAfterCompletion() {
        StartInterviewResult started = interviewService.start(USER_ID);
        interviewService.complete(
                USER_ID,
                started.interviewId(),
                new CompleteInterviewCommand(
                        snapshot("본질문", true),
                        List.of(snapshot("꼬리질문1", true), snapshot("꼬리질문2", true)),
                        0
                )
        );

        assertThatThrownBy(() -> interviewService.start(USER_ID))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_ALREADY_STARTED_TODAY));
    }

    private InterviewAnswerSnapshotCommand snapshot(String questionText, boolean isCorrect) {
        return new InterviewAnswerSnapshotCommand(questionText, "답변", "피드백", "모범답안", isCorrect, null, null);
    }
}
