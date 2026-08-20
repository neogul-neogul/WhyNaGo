package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;
import com.neogul.whynago.question.domain.FollowupScope;
import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.RubricCriterion;
import com.neogul.whynago.question.domain.SolvingTime;
import java.util.List;

record PromptEvaluationCase(
        String id,
        EssayGradingMode mode,
        boolean generateFollowup,
        String question,
        String answer,
        List<RubricCriterionSpec> rubric,
        FollowupScopeSpec followupScope,
        Integer elapsedSeconds,
        Integer averageElapsedSeconds,
        int sampleCount,
        ExpectedEvaluation expected
) {

    EssayGradingTarget target() {
        List<RubricCriterion> criteria = rubric == null ? List.of() : rubric.stream()
                .map(item -> new RubricCriterion(item.point(), item.weight()))
                .toList();
        FollowupScope scope = followupScope == null
                ? null
                : new FollowupScope(followupScope.allowed(), followupScope.forbidden());
        Rubric gradingRubric = criteria.isEmpty() ? null : new Rubric(criteria, scope);
        return new EssayGradingTarget(
                question,
                answer,
                gradingRubric,
                SolvingTime.of(elapsedSeconds, averageElapsedSeconds, sampleCount));
    }

    record RubricCriterionSpec(String point, int weight) {
    }

    record FollowupScopeSpec(List<String> allowed, List<String> forbidden) {
    }

    record ExpectedEvaluation(
            String scoringFocus,
            List<String> feedbackMustCover,
            List<String> followupMustMention,
            List<String> followupMustNotMention
    ) {
    }
}
