package com.neogul.whynago.question.infra.ai.prompt;

import com.neogul.whynago.question.domain.EssayGradingMode;

// v6의 입·출력 형식과 규칙은 그대로 두고, 부분 정답과 모름 답변의 기대 판정을 예시로 보강한다.
// 예시는 고정 답안이 아니라 점수·숙련도·꼬리질문 깊이를 맞추기 위한 few-shot 기준이다.
public class EssayPromptV7 extends EssayPromptV6 {

    private static final String VERSION = "v7";

    private static final String FEW_SHOT_EXAMPLES = """

            [판정 예시]
            아래 예시는 형식과 판단 수준을 맞추기 위한 예시다. 실제 채점 대상에 예시의 개념이나 문장을 억지로 적용하지 마라.

            예시 1 - 부분 정답과 루브릭:
            질문: TCP가 신뢰성 있는 전송을 제공하는 방법을 설명해 보세요.
            답변: 수신 확인을 받고 응답이 없으면 다시 보내기 때문에 신뢰성이 있습니다.
            루브릭: 재전송(4점), 순서 보장(3점), 전송량 조절(3점)
            좋은 판정: score는 4이고, criteriaResults는 재전송만 met=true로 판정한다. mastery는 UNSTABLE이다.
            feedback은 순서 번호와 흐름 제어 또는 혼잡 제어가 빠졌음을 구체적으로 짚는다.
            followupQuestion은 "수신 확인을 받지 못했을 때 송신 측은 어떻게 판단하고 재전송할까요?"처럼
            답변자가 언급한 재전송을 한 단계만 더 확인한다.

            예시 2 - 모름 답변:
            질문: 해시 함수의 용도를 설명해 보세요.
            답변: 잘 모르겠습니다.
            좋은 판정: score는 0, mastery는 NOT_LEARNED이며 masteryReason은 모른다고 밝힌 사실을 짚는다.
            feedback은 해시 함수가 입력을 일정한 길이의 값으로 바꾸는 기본 역할 한 가지부터 제시한다.
            followupQuestion은 모른다고 한 용어를 반복하거나 더 깊게 묻지 말고,
            "긴 데이터를 짧은 대표값으로 바꾸어 비교하면 어떤 점이 편리할까요?"처럼 더 기초적인 관점으로 낮춘다.
            "해시 충돌은 왜"처럼 새 심화 용어를 꺼내는 질문은 좋은 꼬리질문이 아니다.
            """;

    @Override
    public String version() {
        return VERSION;
    }

    @Override
    public String systemPrompt(EssayGradingMode mode) {
        return super.systemPrompt(mode) + FEW_SHOT_EXAMPLES;
    }
}
