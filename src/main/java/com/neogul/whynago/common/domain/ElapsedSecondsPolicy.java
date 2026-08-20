package com.neogul.whynago.common.domain;

// 문항별 소요 시간은 클라이언트가 보고하므로 신뢰하지 않는다.
// 세 개의 저장 경로가 모두 엔티티 정적 팩토리로 수렴하므로 정규화를 그 안에서 호출해,
// 어떤 경로로 들어와도 상한을 우회할 수 없게 한다.
//
// 풀이 저장(solvedsession)과 서술형 채점(question)이 같은 상한을 써야 하므로 특정 도메인에 두지 않는다.
// 두 경로가 다른 상한을 쓰면 채점에 반영된 시간과 기록에 남는 시간이 어긋난다.
//
// 0 이하는 측정 실패로 보고 null(미측정)로 만든다. null과 0은 다른 뜻이다 —
// null은 "시간 신호 없음"이고, 0초는 있을 수 없는 값이다.
public final class ElapsedSecondsPolicy {

    public static final int MAX_SECONDS = 600;

    private ElapsedSecondsPolicy() {
    }

    public static Integer normalize(Integer rawSeconds) {
        if (rawSeconds == null || rawSeconds <= 0) {
            return null;
        }
        return Math.min(rawSeconds, MAX_SECONDS);
    }
}
