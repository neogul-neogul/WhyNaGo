package com.neogul.whynago.mastery.domain;

import com.neogul.whynago.common.domain.ElapsedPace;
import com.neogul.whynago.common.domain.ElapsedPacePolicy;
import com.neogul.whynago.common.domain.MasteryLevel;

// `내 소요시간 / 문제 평균 소요시간`을 정답 여부와 교차해 숙련도를 판정한다.
// 정답률만 보면 "알지만 헤맴"과 "찍어서 틀림"을 구분할 수 없다.
//
// 빠름·느림의 눈금은 ElapsedPacePolicy가 갖는다 - 서술형 채점도 같은 눈금을 써야 하기 때문이다.
public class MasteryPolicy {

    // 서술형 점수(0~10)가 이 이하면 시간과 무관하게 개념이 없다고 본다.
    private static final int NOT_LEARNED_SCORE = 3;

    public MasteryLevel judge(SolvedSignal signal, Integer avgElapsedSeconds, int sampleCount) {
        if (signal.essayScore() != null && signal.essayScore() <= NOT_LEARNED_SCORE) {
            return MasteryLevel.NOT_LEARNED;
        }

        ElapsedPace pace = ElapsedPacePolicy.classify(signal.elapsedSeconds(), avgElapsedSeconds, sampleCount);
        if (signal.correct()) {
            return judgeCorrect(pace);
        }
        return judgeWrong(pace);
    }

    private MasteryLevel judgeCorrect(ElapsedPace pace) {
        return switch (pace) {
            case FAST -> MasteryLevel.MASTERED;
            case NORMAL -> MasteryLevel.SOLID;
            case SLOW -> MasteryLevel.UNSTABLE;
        };
    }

    private MasteryLevel judgeWrong(ElapsedPace pace) {
        return switch (pace) {
            case FAST -> MasteryLevel.GUESSED;
            case NORMAL -> MasteryLevel.WEAK;
            case SLOW -> MasteryLevel.NOT_LEARNED;
        };
    }
}
