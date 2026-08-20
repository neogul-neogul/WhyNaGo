package com.neogul.whynago.question.domain;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.common.domain.ElapsedPacePolicy;
import com.neogul.whynago.common.domain.ElapsedSecondsPolicy;

// 서술형 답변 한 건의 소요시간 신호다. 초 자체가 아니라 **문제 평균 대비 빠름·느림**을 채점에 쓴다.
// 절대 초만으로는 난이도별 편차를 구분할 수 없어, 같은 90초가 쉬운 문항에서는 느리고 어려운 문항에서는 빠르다.
//
// 눈금(ElapsedPacePolicy)은 객관식 숙련도 판정과 공유한다. 같은 소요시간이 두 트랙에서 다르게
// 해석되면 학습 기록과 채점 결과가 서로 다른 말을 한다.
public record SolvingTime(Integer elapsedSeconds, int baselineSeconds, ElapsedPace pace) {

    // 평균보다 빨랐으면 올리고 오래 걸렸으면 내린다. 루브릭 배점(항목당 3~4점)보다 작게 두어
    // 시간이 내용 판정을 뒤집지 못하게 한다.
    private static final int FAST_BONUS = 1;
    private static final int SLOW_PENALTY = -1;

    // 클라이언트가 시간을 보고하지 않았거나 측정에 실패한 경우다. 채점에 시간을 반영하지 않는다.
    public static SolvingTime unmeasured() {
        return new SolvingTime(null, ElapsedPacePolicy.DEFAULT_ELAPSED_SECONDS, ElapsedPace.NORMAL);
    }

    // rawSeconds는 클라이언트가 보고한 값이라 신뢰하지 않고 저장 경로와 같은 정규화를 거친다.
    public static SolvingTime of(Integer rawSeconds, Integer avgElapsedSeconds, int sampleCount) {
        Integer elapsedSeconds = ElapsedSecondsPolicy.normalize(rawSeconds);
        if (elapsedSeconds == null) {
            return unmeasured();
        }
        return new SolvingTime(
                elapsedSeconds,
                ElapsedPacePolicy.baseline(avgElapsedSeconds, sampleCount),
                ElapsedPacePolicy.classify(elapsedSeconds, avgElapsedSeconds, sampleCount));
    }

    public boolean isMeasured() {
        return elapsedSeconds != null;
    }

    public int scoreAdjustment() {
        if (!isMeasured()) {
            return 0;
        }
        return switch (pace) {
            case FAST -> FAST_BONUS;
            case NORMAL -> 0;
            case SLOW -> SLOW_PENALTY;
        };
    }
}
