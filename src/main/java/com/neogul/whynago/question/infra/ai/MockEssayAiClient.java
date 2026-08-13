package com.neogul.whynago.question.infra.ai;

import com.neogul.whynago.question.domain.EssayGradingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class MockEssayAiClient implements EssayAiClient {

    private static final String FEEDBACK = "[MOCK] Gemini 호출 없이 반환되는 로컬 임시 피드백입니다.";
    private static final String MODEL_ANSWER = "[MOCK] 로컬 임시 모범답안입니다.";
    private static final String FOLLOWUP_QUESTION = "[MOCK] 로컬 임시 꼬리질문입니다.";
    private static final int PASS_SCORE = 8;
    private static final int FAIL_SCORE = 3;
    private static final String FAIL_KEYWORD = "모르겠";

    private final Map<String, AtomicInteger> completedTurnsByConversation = new ConcurrentHashMap<>();

    @Override
    public GradeAndFollowupResult gradeAndGenerateFollowup(
            String conversationId,
            String question,
            String answer,
            boolean generateFollowup,
            EssayGradingMode mode
    ) {
        completedTurnsByConversation
                .computeIfAbsent(conversationId, id -> new AtomicInteger())
                .incrementAndGet();

        int score = answer != null && answer.contains(FAIL_KEYWORD) ? FAIL_SCORE : PASS_SCORE;
        return new GradeAndFollowupResult(
                FEEDBACK,
                MODEL_ANSWER,
                score,
                generateFollowup ? FOLLOWUP_QUESTION : null
        );
    }

    @Override
    public int completedTurns(String conversationId) {
        return completedTurnsByConversation.getOrDefault(conversationId, new AtomicInteger()).get();
    }

    @Override
    public void clearSession(String conversationId) {
        completedTurnsByConversation.remove(conversationId);
    }
}
