package com.neogul.whynago.question.infra.ai;

public interface EssayAiClient {

    GradeAndFollowupResult gradeAndGenerateFollowup(
            String conversationId,
            String question,
            String answer,
            boolean generateFollowup
    );

    int completedTurns(String conversationId);

    void clearSession(String conversationId);
}
