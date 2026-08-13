package com.neogul.whynago.question.infra.ai.prompt;

import com.neogul.whynago.question.domain.EssayGradingMode;

public interface EssayPrompt {

    String version();

    String systemPrompt(EssayGradingMode mode);

    String userPrompt(EssayGradingMode mode, String question, String answer, boolean generateFollowup);
}
