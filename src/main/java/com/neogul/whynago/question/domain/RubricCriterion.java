package com.neogul.whynago.question.domain;

// 채점 기준 한 항목. point는 답변이 담아야 할 내용, weight는 그 항목의 배점이다.
public record RubricCriterion(String point, int weight) {
}
