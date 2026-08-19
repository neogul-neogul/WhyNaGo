package com.neogul.whynago.question.implement.dto;

// 객관식 풀이 1건 중 숙련도 판정에 필요한 값만 담는다.
// elapsedSeconds는 클라이언트가 보고하지 않으면 null이며, 0이 아니라 "신호 없음"이다.
public record ChoiceSolveSignal(
        Long questionId,
        boolean correct,
        Integer elapsedSeconds
) {
}
