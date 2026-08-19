package com.neogul.whynago.mastery.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.common.domain.MasteryLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MasteryPolicyTest {

    private static final int RELIABLE_SAMPLE = 5;
    private static final int AVG_ELAPSED_SECONDS = 100;

    private final MasteryPolicy masteryPolicy = new MasteryPolicy();

    @Test
    @DisplayName("평균보다 빠르게 맞히면 완전히 익힌 것으로 본다.")
    void judge_fastCorrect() {
        assertThat(judge(true, 60)).isEqualTo(MasteryLevel.MASTERED);
    }

    @Test
    @DisplayName("평균과 비슷한 시간에 맞히면 안정적으로 본다.")
    void judge_normalCorrect() {
        assertThat(judge(true, 100)).isEqualTo(MasteryLevel.SOLID);
    }

    @Test
    @DisplayName("맞혔지만 평균보다 오래 걸리면 불안정으로 본다.")
    void judge_slowCorrect() {
        assertThat(judge(true, 200)).isEqualTo(MasteryLevel.UNSTABLE);
    }

    @Test
    @DisplayName("평균보다 빠르게 틀리면 찍은 것으로 본다.")
    void judge_fastWrong() {
        assertThat(judge(false, 60)).isEqualTo(MasteryLevel.GUESSED);
    }

    @Test
    @DisplayName("평균과 비슷한 시간에 틀리면 취약으로 본다.")
    void judge_normalWrong() {
        assertThat(judge(false, 100)).isEqualTo(MasteryLevel.WEAK);
    }

    @Test
    @DisplayName("오래 걸리고도 틀리면 개념이 없는 것으로 본다.")
    void judge_slowWrong() {
        assertThat(judge(false, 200)).isEqualTo(MasteryLevel.NOT_LEARNED);
    }

    @Test
    @DisplayName("빠름 경계값은 빠름에 포함하지 않는다.")
    void judge_fastBoundary() {
        // ratio = 0.7 정확히. 빠름 조건은 미만이므로 보통이다.
        assertThat(judge(true, 70)).isEqualTo(MasteryLevel.SOLID);
    }

    @Test
    @DisplayName("느림 경계값은 느림에 포함하지 않는다.")
    void judge_slowBoundary() {
        // ratio = 1.5 정확히. 느림 조건은 초과이므로 보통이다.
        assertThat(judge(true, 150)).isEqualTo(MasteryLevel.SOLID);
    }

    @Test
    @DisplayName("서술형 점수가 3점 이하면 시간과 무관하게 개념이 없는 것으로 본다.")
    void judge_lowEssayScore() {
        SolvedSignal signal = SolvedSignal.of(true, 3, 10);

        assertThat(masteryPolicy.judge(signal, AVG_ELAPSED_SECONDS, RELIABLE_SAMPLE))
                .isEqualTo(MasteryLevel.NOT_LEARNED);
    }

    @Test
    @DisplayName("서술형 점수가 4점 이상이면 시간으로 판정한다.")
    void judge_essayScoreAboveThreshold() {
        SolvedSignal signal = SolvedSignal.of(true, 4, 10);

        assertThat(masteryPolicy.judge(signal, AVG_ELAPSED_SECONDS, RELIABLE_SAMPLE))
                .isEqualTo(MasteryLevel.MASTERED);
    }

    @Test
    @DisplayName("표본이 5건 미만이면 평균을 신뢰하지 않고 기본값 180초를 기준으로 판정한다.")
    void judge_unreliableSample() {
        SolvedSignal signal = SolvedSignal.of(true, null, 120);

        // 평균 100초를 믿으면 ratio 1.2로 보통이지만, 기본값 180초 기준이면 ratio 0.67로 빠름이다.
        assertThat(masteryPolicy.judge(signal, AVG_ELAPSED_SECONDS, RELIABLE_SAMPLE - 1))
                .isEqualTo(MasteryLevel.MASTERED);
    }

    @Test
    @DisplayName("문항 평균이 없으면 기본값 180초를 기준으로 판정한다.")
    void judge_withoutQuestionAverage() {
        SolvedSignal signal = SolvedSignal.of(true, null, 120);

        assertThat(masteryPolicy.judge(signal, null, RELIABLE_SAMPLE)).isEqualTo(MasteryLevel.MASTERED);
    }

    @Test
    @DisplayName("소요 시간이 없으면 빠름·느림을 판정하지 않고 보통으로 본다.")
    void judge_withoutElapsedSeconds() {
        SolvedSignal signal = SolvedSignal.of(false, null, null);

        assertThat(masteryPolicy.judge(signal, AVG_ELAPSED_SECONDS, RELIABLE_SAMPLE)).isEqualTo(MasteryLevel.WEAK);
    }

    private MasteryLevel judge(boolean correct, int elapsedSeconds) {
        return masteryPolicy.judge(
                SolvedSignal.of(correct, null, elapsedSeconds),
                AVG_ELAPSED_SECONDS,
                RELIABLE_SAMPLE
        );
    }
}
