package com.neogul.whynago.question.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.common.domain.ElapsedPacePolicy;
import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.domain.SolvingTime;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SolvingTimeReaderTest {

    private static final Long QUESTION_ID = 7L;

    private final QuestionStatReader questionStatReader = Mockito.mock(QuestionStatReader.class);
    private final SolvingTimeReader solvingTimeReader = new SolvingTimeReader(questionStatReader);

    @Test
    @DisplayName("문항 평균을 기준선으로 삼아 빠름·느림을 판정한다.")
    void read_ignoresQuestionAverage() {
        given(questionStatReader.read(QUESTION_ID)).willReturn(Optional.of(stat(400, 20)));

        SolvingTime solvingTime = solvingTimeReader.read(QUESTION_ID, 200);

        assertThat(solvingTime.baselineSeconds()).isEqualTo(400);
        assertThat(solvingTime.pace())
                .as("평균 400초 문항의 200초는 빠른 것이다")
                .isEqualTo(ElapsedPace.FAST);
    }

    @Test
    @DisplayName("문항 통계가 없으면 기준 시간을 기준선으로 삼는다.")
    void read_withoutStat() {
        given(questionStatReader.read(QUESTION_ID)).willReturn(Optional.empty());

        SolvingTime solvingTime = solvingTimeReader.read(QUESTION_ID, 200);

        assertThat(solvingTime.baselineSeconds()).isEqualTo(ElapsedPacePolicy.DEFAULT_ELAPSED_SECONDS);
        assertThat(solvingTime.isMeasured()).isTrue();
    }

    @Test
    @DisplayName("시간을 보고하지 않으면 문항 통계를 조회하지 않는다.")
    void read_readsStatWithoutTime() {
        SolvingTime solvingTime = solvingTimeReader.read(QUESTION_ID, null);

        assertThat(solvingTime.isMeasured()).isFalse();
        verify(questionStatReader, never()).read(QUESTION_ID);
    }

    private static QuestionStat stat(Integer avgElapsedSeconds, int sampleCount) {
        return QuestionStat.of(QUESTION_ID, avgElapsedSeconds, 0.5, sampleCount, LocalDateTime.now());
    }
}
