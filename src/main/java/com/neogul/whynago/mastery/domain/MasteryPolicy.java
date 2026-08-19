package com.neogul.whynago.mastery.domain;

import com.neogul.whynago.common.domain.MasteryLevel;

// `내 소요시간 / 문제 평균 소요시간`을 정답 여부와 교차해 숙련도를 판정한다.
// 정답률만 보면 "알지만 헤맴"과 "찍어서 틀림"을 구분할 수 없다.
public class MasteryPolicy {

    private static final double FAST_RATIO = 0.7;
    private static final double SLOW_RATIO = 1.5;
    // 표본이 이보다 적으면 평균을 신뢰하지 않는다.
    private static final int MIN_RELIABLE_SAMPLE = 5;
    // 신뢰할 평균이 없을 때 쓰는 기준 시간(초).
    private static final int DEFAULT_ELAPSED_SECONDS = 180;
    // 서술형 점수(0~10)가 이 이하면 시간과 무관하게 개념이 없다고 본다.
    private static final int NOT_LEARNED_SCORE = 3;

    public MasteryLevel judge(SolvedSignal signal, Integer avgElapsedSeconds, int sampleCount) {
        if (signal.essayScore() != null && signal.essayScore() <= NOT_LEARNED_SCORE) {
            return MasteryLevel.NOT_LEARNED;
        }

        double ratio = ratio(signal.elapsedSeconds(), avgElapsedSeconds, sampleCount);
        if (signal.correct()) {
            return judgeCorrect(ratio);
        }
        return judgeWrong(ratio);
    }

    private MasteryLevel judgeCorrect(double ratio) {
        if (ratio < FAST_RATIO) {
            return MasteryLevel.MASTERED;
        }
        if (ratio > SLOW_RATIO) {
            return MasteryLevel.UNSTABLE;
        }
        return MasteryLevel.SOLID;
    }

    private MasteryLevel judgeWrong(double ratio) {
        if (ratio < FAST_RATIO) {
            return MasteryLevel.GUESSED;
        }
        if (ratio > SLOW_RATIO) {
            return MasteryLevel.NOT_LEARNED;
        }
        return MasteryLevel.WEAK;
    }

    // 내 소요 시간이 없으면 빠름·느림을 말할 수 없으므로 보통(1.0)으로 둔다.
    private double ratio(Integer elapsedSeconds, Integer avgElapsedSeconds, int sampleCount) {
        if (elapsedSeconds == null) {
            return 1.0;
        }
        return (double) elapsedSeconds / baseline(avgElapsedSeconds, sampleCount);
    }

    private int baseline(Integer avgElapsedSeconds, int sampleCount) {
        if (avgElapsedSeconds == null || avgElapsedSeconds <= 0 || sampleCount < MIN_RELIABLE_SAMPLE) {
            return DEFAULT_ELAPSED_SECONDS;
        }
        return avgElapsedSeconds;
    }
}
