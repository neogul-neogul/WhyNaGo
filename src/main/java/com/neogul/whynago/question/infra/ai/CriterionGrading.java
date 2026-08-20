package com.neogul.whynago.question.infra.ai;

// 루브릭 항목 하나에 대한 AI 판정. index는 프롬프트에 내려준 1부터 시작하는 항목 번호다.
// 항목 문장을 되돌려받지 않고 번호만 받아, 서버가 번호로 원본 항목과 이어 붙인다.
public record CriterionGrading(int index, boolean met, String reason) {
}
