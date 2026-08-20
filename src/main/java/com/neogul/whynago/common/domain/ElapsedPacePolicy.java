package com.neogul.whynago.common.domain;

// `내 소요시간 / 문제 평균 소요시간`으로 빠름·보통·느림을 가른다.
// 임계값과 기준 시간을 한 곳에 두어 객관식 숙련도 판정과 서술형 채점이 같은 눈금을 쓰게 한다.
public final class ElapsedPacePolicy {

    private static final double FAST_RATIO = 0.7;
    private static final double SLOW_RATIO = 1.5;
    // 표본이 이보다 적으면 평균을 신뢰하지 않는다.
    private static final int MIN_RELIABLE_SAMPLE = 5;
    // 신뢰할 평균이 없을 때 쓰는 기준 시간(초).
    public static final int DEFAULT_ELAPSED_SECONDS = 180;

    private ElapsedPacePolicy() {
    }

    public static ElapsedPace classify(Integer elapsedSeconds, Integer avgElapsedSeconds, int sampleCount) {
        double ratio = ratio(elapsedSeconds, avgElapsedSeconds, sampleCount);
        if (ratio < FAST_RATIO) {
            return ElapsedPace.FAST;
        }
        if (ratio > SLOW_RATIO) {
            return ElapsedPace.SLOW;
        }
        return ElapsedPace.NORMAL;
    }

    // 표본이 부족하거나 평균이 없으면 기준 시간으로 대체한다.
    public static int baseline(Integer avgElapsedSeconds, int sampleCount) {
        if (avgElapsedSeconds == null || avgElapsedSeconds <= 0 || sampleCount < MIN_RELIABLE_SAMPLE) {
            return DEFAULT_ELAPSED_SECONDS;
        }
        return avgElapsedSeconds;
    }

    // 내 소요 시간이 없으면 빠름·느림을 말할 수 없으므로 보통(1.0)으로 둔다.
    private static double ratio(Integer elapsedSeconds, Integer avgElapsedSeconds, int sampleCount) {
        if (elapsedSeconds == null) {
            return 1.0;
        }
        return (double) elapsedSeconds / baseline(avgElapsedSeconds, sampleCount);
    }
}
