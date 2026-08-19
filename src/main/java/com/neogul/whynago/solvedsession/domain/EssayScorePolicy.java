package com.neogul.whynago.solvedsession.domain;

// 서술형 채점 점수는 AI 채점 응답을 클라이언트가 중계하므로 신뢰하지 않는다.
// 소요 시간과 같은 이유로 엔티티 정적 팩토리 안에서 정규화한다.
//
// null은 "점수 신호 없음"이며 0점이 아니다. 이 구분이 무너지면 추천의 숙련도 판정에서
// 점수 미보고 문항이 전부 최저점으로 취급된다.
public final class EssayScorePolicy {

    public static final int MIN_SCORE = 0;
    public static final int MAX_SCORE = 10;

    private EssayScorePolicy() {
    }

    public static Integer normalize(Integer rawScore) {
        if (rawScore == null) {
            return null;
        }
        return Math.clamp(rawScore, MIN_SCORE, MAX_SCORE);
    }
}
