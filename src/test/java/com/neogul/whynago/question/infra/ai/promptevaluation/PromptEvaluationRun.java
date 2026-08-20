package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.util.List;

record PromptEvaluationRun(
        PromptEvaluationCase testCase,
        EssayPrompt prompt,
        GradeAndFollowupResult response,
        PromptEvaluationJudge.JudgeResult judge,
        List<String> contractFailures
) {

    String caseId() {
        return testCase.id();
    }

    boolean contractPassed() {
        return contractFailures.isEmpty();
    }
}
