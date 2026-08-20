package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.question.domain.EssayGradingMode;
import com.neogul.whynago.question.domain.EssayGradingTarget;

public interface EssayAiClient {

    GradeAndFollowupResult gradeAndGenerateFollowup(
            String conversationId,
            EssayGradingTarget target,
            boolean generateFollowup,
            EssayGradingMode mode
    );

    int completedTurns(String conversationId);

    void clearSession(String conversationId);
}
