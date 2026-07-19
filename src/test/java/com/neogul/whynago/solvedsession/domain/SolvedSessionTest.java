package com.neogul.whynago.solvedsession.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.QuestionType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SolvedSessionTest {

    @Test
    @DisplayName("완료 세션을 생성한다.")
    void completed() {
        LocalDateTime solvedAt = LocalDateTime.now();

        SolvedSession solvedSession = SolvedSession.completed(
                1L,
                QuestionType.MULTIPLE_CHOICE,
                2,
                1,
                solvedAt
        );

        assertThat(solvedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(solvedSession.getTotalCount()).isEqualTo(2);
        assertThat(solvedSession.getCorrectCount()).isEqualTo(1);
        assertThat(solvedSession.getSolvedAt()).isEqualTo(solvedAt);
        assertThat(solvedSession.getCreatedAt()).isNotNull();
    }
}
