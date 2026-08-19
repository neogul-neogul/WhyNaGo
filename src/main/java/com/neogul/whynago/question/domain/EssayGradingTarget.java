package com.neogul.whynago.question.domain;

// 서술형 한 턴을 채점하는 데 필요한 입력 묶음이다. 프롬프트가 "[채점 대상]"이라 부르는 그 단위다.
//
// 하나씩 인자로 넘기면 nullable 값이 섞인 일곱 자리 시그니처가 되어 호출부에서 순서를 틀리기 쉽다.
// rubric은 루브릭이 없는 문항·꼬리질문 턴에서 null이고, solvingTime은 미측정일 수 있다.
public record EssayGradingTarget(String question, String answer, Rubric rubric, SolvingTime solvingTime) {

    public boolean hasRubric() {
        return rubric != null && !rubric.isEmpty();
    }

    // 꼬리질문 턴에는 루브릭을 내려보내지 않는다. 세션마다 AI가 만든 발문이라 채점 기준이 없다.
    public EssayGradingTarget withoutRubric() {
        return new EssayGradingTarget(question, answer, null, solvingTime);
    }
}
