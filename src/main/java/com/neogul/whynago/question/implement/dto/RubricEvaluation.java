package com.neogul.whynago.question.implement.dto;

// 루브릭 항목 하나의 채점 결과. AI가 번호로 돌려준 판정을 서버가 원본 항목(point·weight)과 이어 붙인 것이다.
public record RubricEvaluation(String point, int weight, boolean met, String reason) {
}
