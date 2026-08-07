package com.neogul.whynago.interview.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.DailyInterviewFixture;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.question.domain.Category;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DailyInterviewTest {

    @Test
    @DisplayName("면접을 시작하면 진행 중 상태로 만들어진다.")
    void start() {
        LocalDateTime startedAt = LocalDateTime.of(2026, 8, 7, 9, 20, 0);

        DailyInterview interview = DailyInterview.start(
                10L,
                LocalDate.of(2026, 8, 7),
                Category.NETWORK,
                7L,
                "conversation-id",
                startedAt
        );

        assertThat(interview.getUserId()).isEqualTo(10L);
        assertThat(interview.getInterviewDate()).isEqualTo(LocalDate.of(2026, 8, 7));
        assertThat(interview.getCategory()).isEqualTo(Category.NETWORK);
        assertThat(interview.getQuestionId()).isEqualTo(7L);
        assertThat(interview.getConversationId()).isEqualTo("conversation-id");
        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.IN_PROGRESS);
        assertThat(interview.getStartedAt()).isEqualTo(startedAt);
        assertThat(interview.getSolvedSessionId()).isNull();
        assertThat(interview.getCompletedAt()).isNull();
        assertThat(interview.getFocusLossCount()).isZero();
    }

    @Test
    @DisplayName("면접을 완료하면 완료 상태로 바뀌고 풀이 세션과 이탈 횟수가 기록된다.")
    void complete() {
        DailyInterview interview = DailyInterviewFixture.inProgress(10L);
        LocalDateTime completedAt = LocalDateTime.of(2026, 8, 7, 9, 31, 40);

        interview.complete(42L, 3, completedAt);

        assertThat(interview.getStatus()).isEqualTo(InterviewStatus.COMPLETED);
        assertThat(interview.getSolvedSessionId()).isEqualTo(42L);
        assertThat(interview.getFocusLossCount()).isEqualTo(3);
        assertThat(interview.getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    @DisplayName("이미 완료된 면접을 다시 완료하면 예외가 발생한다.")
    void completeAlreadyCompleted() {
        DailyInterview interview = DailyInterviewFixture.completed(10L);

        assertThatThrownBy(() -> interview.complete(43L, 0, LocalDateTime.now()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_NOT_IN_PROGRESS));
    }

    @Test
    @DisplayName("진행 중이고 채점된 턴이 없으면 취소할 수 있다.")
    void isCancelable() {
        DailyInterview interview = DailyInterviewFixture.inProgress(10L);

        assertThat(interview.isCancelable(0)).isTrue();
    }

    @Test
    @DisplayName("채점된 턴이 하나라도 있으면 취소할 수 없다.")
    void isCancelableWithGradedTurn() {
        DailyInterview interview = DailyInterviewFixture.inProgress(10L);

        assertThat(interview.isCancelable(1)).isFalse();
    }

    @Test
    @DisplayName("이미 완료된 면접은 취소할 수 없다.")
    void isCancelableWhenCompleted() {
        DailyInterview interview = DailyInterviewFixture.completed(10L);

        assertThat(interview.isCancelable(0)).isFalse();
    }

    @Test
    @DisplayName("면접 소유자를 확인한다.")
    void isOwnedBy() {
        DailyInterview interview = DailyInterviewFixture.inProgress(10L);

        assertThat(interview.isOwnedBy(10L)).isTrue();
        assertThat(interview.isOwnedBy(11L)).isFalse();
    }
}
