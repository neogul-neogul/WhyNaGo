package com.neogul.whynago.mastery.domain;

import com.neogul.whynago.common.domain.MasteryLevel;

// 객관식 규칙 판정의 근거 문구다. 서술형은 AI가 답변 내용을 짚어 근거를 쓰지만, 객관식은 답변 내용이
// 없으므로 판정에 실제로 쓴 두 신호(정답 여부 · 평균 대비 소요시간)를 그대로 문장으로 옮긴다.
// MasteryPolicy가 객관식에 대해 낼 수 있는 6개 판정과 1:1로 대응한다.
public final class ChoiceMasteryReason {

    private ChoiceMasteryReason() {
    }

    public static String of(MasteryLevel level) {
        return switch (level) {
            case MASTERED -> "정답을 평균보다 빠르게 골랐다.";
            case SOLID -> "정답을 평균 수준의 시간에 골랐다.";
            case UNSTABLE -> "정답이지만 평균보다 오래 걸렸다.";
            case GUESSED -> "오답을 평균보다 빠르게 골랐다.";
            case WEAK -> "오답이고 소요 시간은 평균 수준이다.";
            case NOT_LEARNED -> "오답이고 평균보다 오래 걸렸다.";
        };
    }
}
