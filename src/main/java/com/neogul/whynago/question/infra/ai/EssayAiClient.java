package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.question.domain.EssayGradingMode;

public interface EssayAiClient {

    GradeAndFollowupResult gradeAndGenerateFollowup(
            String conversationId,
            String question,
            String answer,
            boolean generateFollowup,
            EssayGradingMode mode
    );

    int completedTurns(String conversationId);

    void clearSession(String conversationId);
}
