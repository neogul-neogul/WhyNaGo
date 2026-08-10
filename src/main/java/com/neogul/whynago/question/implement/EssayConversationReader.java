package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.infra.ai.EssayAiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssayConversationReader {

    private final EssayAiClient essayAiClient;

    public int completedTurns(String conversationId) {
        return essayAiClient.completedTurns(conversationId);
    }
}
