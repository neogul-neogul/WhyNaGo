package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV6;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV7;
import com.neogul.whynago.question.infra.ai.prompt.EssayPromptV8;
import java.util.LinkedHashMap;
import java.util.Map;

// 한 번의 실행은 언제나 두 프롬프트만 비교한다. 여기 여러 쌍을 등록해 두는 것은
// 쌍마다 핵심 차이 설명이 다르기 때문이고, 실행할 때 그중 하나를 골라 쓴다.
//
// 직전 버전과의 비교(v7 -> v8)는 이번 변경만 떼어 보고, 운영 버전과의 비교(v6 -> v8)는
// 지금 배포된 것을 이걸로 갈아탈지를 본다. 둘은 다른 질문이라 리포트도 따로 남긴다.
final class PromptEvaluationCatalog {

    static final String DEFAULT_COMPARISON = "v7-v8";

    private static final int OLDEST_COMPARABLE_VERSION = 6;

    // 핵심 차이는 프롬프트 문구를 옮긴 것이 아니라, 그 변경이 응답에서 어떤 차이로 나타나야 하는지다.
    // 심사 모델이 응답만 보고 확인할 수 있는 말로 쓴다.
    private static final String V7_TO_V8_DIFFERENCE = """
            기준선은 꼬리질문을 '직전 답변에서 한 단계만 더 깊게'라는 한 가지 규칙으로만 만든다.
            채점 결과가 어떻든 꼬리질문의 깊이는 거의 달라지지 않는다.

            후보는 같은 호출에서 방금 내린 두 판정에 꼬리질문을 맞추게 한다.
            - 루브릭 판정(criteriaResults): 충족하지 못한 항목 중 답변자가 가장 가까이 다가간 하나를 골라
              그 내용을 스스로 떠올리게 묻는다. 모든 항목을 충족했으면 되묻지 않고 적용 상황으로 넓힌다.
            - 숙련도 판정(mastery): 등급마다 깊이를 달리한다. 높은 등급에서는 경계·예외·트레이드오프까지 묻고,
              낮은 등급(GUESSED·WEAK·NOT_LEARNED)에서는 깊이를 더하지 않고 답변자가 딛고 설 지점을 찾는다.

            - 모름 답변: 기준선은 눈높이를 낮추라고만 해서 여전히 맨바닥에서 다시 묻는다.
              후보는 그 문항의 채점 기준 중 충족하지 못한 기초 항목 하나를 전문 용어 없이 한 줄로 풀어
              꼬리질문 앞에 붙이고, 그 한 줄만으로 답할 수 있는 질문을 잇는다.
              풀어 준 한 줄은 항목 문장이나 용어 정의를 그대로 옮긴 것이 아니어야 하고,
              지금 채점 중인 문항의 내용이어야 한다. 다른 문항의 개념이 섞여 나오면 안 된다.
              그래서 모름 답변의 꼬리질문은 서술문 한 문장 뒤에 질문 한 문장이 오는 두 문장 형태로 나와야 한다.

            따라서 후보의 꼬리질문은 그 답변의 채점 결과와 이해 수준에 맞춰 깊이가 달라져야 하고,
            masteryReason이 짚은 빈틈과 같은 곳을 향해야 하며, 모름 답변에는 답할 실마리가 함께 있어야 한다.""";

    // v6 -> v8은 두 번의 변경이 누적된 차이다. v7이 더한 판정 기준선(few-shot)과
    // v8이 더한 꼬리질문 개인화를 함께 본다.
    private static final String V6_TO_V8_DIFFERENCE = """
            기준선은 채점 규칙만 글로 주고 예시를 주지 않으며, 꼬리질문도
            '직전 답변에서 한 단계만 더 깊게'라는 한 가지 규칙으로만 만든다.
            그래서 부분 정답과 모름 답변에서 점수·숙련도의 기준이 흔들리고,
            채점 결과가 어떻든 꼬리질문의 깊이는 거의 달라지지 않는다.

            후보는 두 가지가 더해졌다.
            - 부분 정답과 모름 답변의 기대 판정을 예시로 못 박았다. 부분 정답은 충족한 항목만 인정하고,
              모른다고 밝힌 답변은 0점·NOT_LEARNED로 두며 그 지점을 더 깊이 파고들지 않는다.
            - 꼬리질문을 같은 호출에서 방금 내린 두 판정에 맞춘다. 충족하지 못한 루브릭 항목 중
              답변자가 가장 가까이 다가간 하나를 스스로 떠올리게 묻고, 모든 항목을 충족했으면
              되묻지 않고 적용 상황으로 넓힌다. 숙련도가 낮은 등급이면 깊이를 더하지 않고
              답변자가 딛고 설 지점을 찾는다. 모른다고 밝힌 답변에는 그 문항의 채점 기준 중
              충족하지 못한 기초 항목 하나를 전문 용어 없이 한 줄로 풀어 꼬리질문 앞에 붙이고,
              그 한 줄만으로 답할 수 있는 질문을 잇는다. 다른 문항의 개념이 섞여 나오면 안 된다.
              그래서 모름 답변의 꼬리질문은 서술문 한 문장 뒤에 질문 한 문장이 오는 두 문장 형태로 나와야 한다.

            따라서 후보는 답변 수준에 따라 점수·숙련도가 더 분명하게 갈리고,
            꼬리질문의 깊이도 그 판정에 맞춰 달라져야 하며, 모름 답변에는 답할 실마리가 함께 있어야 한다.""";

    private static final Map<String, PromptComparison> COMPARISONS = comparisons();

    private PromptEvaluationCatalog() {
    }

    private static Map<String, PromptComparison> comparisons() {
        Map<String, PromptComparison> registered = new LinkedHashMap<>();
        registered.put("v7-v8", new PromptComparison(new EssayPromptV7(), new EssayPromptV8(), V7_TO_V8_DIFFERENCE));
        registered.put("v6-v8", new PromptComparison(new EssayPromptV6(), new EssayPromptV8(), V6_TO_V8_DIFFERENCE));
        return Map.copyOf(registered);
    }

    static PromptComparison comparison(String name) {
        PromptComparison comparison = COMPARISONS.get(name);
        if (comparison == null) {
            throw new IllegalStateException("등록되지 않은 비교입니다: %s (등록된 비교: %s)"
                    .formatted(name, COMPARISONS.keySet()));
        }
        return comparison;
    }

    static void validate(PromptComparison comparison) {
        int baseline = versionNumber(comparison.baseline());
        int candidate = versionNumber(comparison.candidate());
        if (baseline == candidate) {
            throw new IllegalStateException("서로 다른 두 프롬프트 버전을 비교해야 합니다: " + comparison.baselineVersion());
        }
        if (candidate < baseline) {
            throw new IllegalStateException("후보는 기준선보다 나중 버전이어야 합니다: %s -> %s"
                    .formatted(comparison.baselineVersion(), comparison.candidateVersion()));
        }
        if (comparison.difference() == null || comparison.difference().isBlank()) {
            throw new IllegalStateException("두 프롬프트의 핵심 차이를 비워 둘 수 없습니다.");
        }
    }

    // v1~v5는 GradeAndFollowupResult 형식이 달라 비교하지 않는다.
    private static int versionNumber(EssayPrompt prompt) {
        String version = prompt.version();
        if (!version.matches("v\\d+")) {
            throw new IllegalStateException("프롬프트 버전 형식이 올바르지 않습니다: " + version);
        }
        int number = Integer.parseInt(version.substring(1));
        if (number < OLDEST_COMPARABLE_VERSION) {
            throw new IllegalStateException("v6 이후 프롬프트만 비교할 수 있습니다: " + version);
        }
        return number;
    }
}
