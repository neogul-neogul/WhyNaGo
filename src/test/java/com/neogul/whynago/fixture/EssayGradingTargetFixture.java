package com.neogul.whynago.fixture;

import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.SolvingTime;

// 대부분의 테스트는 루브릭·소요시간에 관심이 없어 둘 다 없는 채점 대상을 쓴다.
public final class EssayGradingTargetFixture {

    private static final String QUESTION = "질문";
    private static final String ANSWER = "답변";

    private EssayGradingTargetFixture() {
    }

    public static EssayGradingTarget plain() {
        return of(QUESTION, ANSWER);
    }

    public static EssayGradingTarget of(String question, String answer) {
        return new EssayGradingTarget(question, answer, null, SolvingTime.unmeasured());
    }

    public static EssayGradingTarget withRubric(Rubric rubric) {
        return new EssayGradingTarget(QUESTION, ANSWER, rubric, SolvingTime.unmeasured());
    }

    public static EssayGradingTarget withSolvingTime(SolvingTime solvingTime) {
        return new EssayGradingTarget(QUESTION, ANSWER, null, solvingTime);
    }

    public static EssayGradingTarget of(Rubric rubric, SolvingTime solvingTime) {
        return new EssayGradingTarget(QUESTION, ANSWER, rubric, solvingTime);
    }
}
