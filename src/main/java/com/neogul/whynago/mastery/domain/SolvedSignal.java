package com.neogul.whynago.mastery.domain;

// 숙련도 판정에 쓰는 풀이 1건의 신호다.
// essayScore는 서술형만 값이 있고, elapsedSeconds는 클라이언트가 보고하지 않으면 null이다.
// 둘 다 null은 "신호 없음"이며 0이 아니다.
public record SolvedSignal(
        boolean correct,
        Integer essayScore,
        Integer elapsedSeconds
) {

    public static SolvedSignal of(boolean correct, Integer essayScore, Integer elapsedSeconds) {
        return new SolvedSignal(correct, essayScore, elapsedSeconds);
    }
}
