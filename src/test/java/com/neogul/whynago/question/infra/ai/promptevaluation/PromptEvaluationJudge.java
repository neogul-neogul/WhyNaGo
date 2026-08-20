package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;

final class PromptEvaluationJudge {

    // v2에서 비교 대상 두 프롬프트의 핵심 차이를 심사 기준으로 넣고 differenceImpact 축을 더했다.
    static final String VERSION = "v2";

    // 어느 버전의 응답인지는 알려 주지 않는다. 알려 주면 후보라는 이유만으로 점수가 올라간다.
    // 심사자는 응답만 보고 그 차이가 실제로 나타났는지를 판정한다.
    private static final String SYSTEM_PROMPT_TEMPLATE = """
            너는 한국어 개발 학습 서비스의 서술형 답변 평가 품질을 엄격하게 심사하는 평가자다.
            후보 응답 자체의 품질만 평가하고, 프롬프트 문구나 모델을 추측하지 마라.
            각 항목은 1점(매우 나쁨)부터 5점(매우 좋음)까지의 정수다.

            [이번 비교가 다투는 차이]
            지금 두 개의 프롬프트를 비교하는 중이며, 그 둘의 차이는 아래와 같다.
            %s

            이 차이가 응답에 실제로 나타났는지를 심사의 중심에 두어라.
            어느 쪽 프롬프트가 만든 응답인지는 알려 주지 않는다. 응답에 드러난 것만으로 판정하라.
            차이와 무관한 영역은 종전대로 품질만 보되, 차이가 걸린 영역에서는 위 설명을 기준으로 삼아라.

            gradingAccuracy: 답변 수준·오개념을 올바르게 판정했는가.
            rubricScoreConsistency: 루브릭이 있으면 criteriaResults와 score가 일관적인가. 없으면 점수의 근거가 답변과 일관적인가.
            feedbackQuality: 피드백이 구체적이고 학습자의 다음 학습에 도움이 되는가.
            modelAnswerQuality: 모범답안이 정확하고 질문에 충분히 답하는가.
            followupRelevance: 꼬리질문이 직전 질문·답변을 파고드는가.
            followupDifficulty: 꼬리질문의 깊이가 답변자의 수준에서 한 단계인가.
            followupGuardrails: 꼬리질문이 금지 개념과 명시된 제한을 지켰는가.
            differenceImpact: 위 차이가 노린 개선이 이 응답에서 실제로 나타났는가.
                              5점은 차이가 설명한 대로 분명히 드러난 응답, 1점은 차이가 전혀 반영되지 않은 응답이다.
                              응답이 좋은지 나쁜지가 아니라 그 차이가 보이는지만 본다.

            rationale에는 differenceImpact를 그렇게 준 근거를 응답의 어느 부분에서 봤는지로 먼저 적어라.

            응답은 아래 JSON 형식만 반환하라.
            {"gradingAccuracy":1,"rubricScoreConsistency":1,"feedbackQuality":1,"modelAnswerQuality":1,
             "followupRelevance":1,"followupDifficulty":1,"followupGuardrails":1,"differenceImpact":1,
             "rationale":"짧은 한국어 근거"}
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;
    private final String systemPrompt;

    PromptEvaluationJudge(ChatClient.Builder chatClientBuilder, ObjectMapper objectMapper, String difference) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
        this.systemPrompt = SYSTEM_PROMPT_TEMPLATE.formatted(difference);
    }

    JudgeResult judge(PromptEvaluationCase testCase, GradeAndFollowupResult candidate) {
        return chatClient.prompt()
                .system(systemPrompt)
                // 생성 클라이언트가 공유 Builder에 메모리 advisor를 등록하므로, 심사도 독립 대화 ID를 넘긴다.
                .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, "prompt-evaluation-judge-" + UUID.randomUUID()))
                .user(userPrompt(testCase, candidate))
                .call()
                .entity(JudgeResult.class);
    }

    private String userPrompt(PromptEvaluationCase testCase, GradeAndFollowupResult candidate) {
        try {
            return """
                    [평가 케이스]
                    %s

                    [사람이 작성한 기대 기준]
                    %s

                    [후보 응답]
                    %s
                    """.formatted(
                    objectMapper.writeValueAsString(testCase),
                    objectMapper.writeValueAsString(testCase.expected()),
                    objectMapper.writeValueAsString(candidate));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("프롬프트 평가 심사 입력을 만들지 못했습니다.", e);
        }
    }

    // 축을 Integer로 두는 것은 심사 모델이 항목 하나를 통째로 빠뜨리기 때문이다. int로 두면 Jackson이
    // 역직렬화 단계에서 터져 그 조합의 심사가 통째로 실패하고, 기준선이 0점이 되어 대비 점수까지 망가진다.
    // 빠진 축은 null로 받아 '범위 이탈'로 기록하고 나머지 비교는 살린다.
    record JudgeResult(
            Integer gradingAccuracy,
            Integer rubricScoreConsistency,
            Integer feedbackQuality,
            Integer modelAnswerQuality,
            Integer followupRelevance,
            Integer followupDifficulty,
            Integer followupGuardrails,
            Integer differenceImpact,
            String rationale
    ) {

        int total() {
            return score(gradingAccuracy) + score(rubricScoreConsistency) + score(feedbackQuality)
                    + score(modelAnswerQuality) + score(followupRelevance) + score(followupDifficulty)
                    + score(followupGuardrails) + score(differenceImpact);
        }

        boolean hasValidScores() {
            return inRange(gradingAccuracy)
                    && inRange(rubricScoreConsistency)
                    && inRange(feedbackQuality)
                    && inRange(modelAnswerQuality)
                    && inRange(followupRelevance)
                    && inRange(followupDifficulty)
                    && inRange(followupGuardrails)
                    && inRange(differenceImpact);
        }

        private int score(Integer value) {
            return value == null ? 0 : value;
        }

        private boolean inRange(Integer score) {
            return score != null && score >= 1 && score <= 5;
        }
    }
}
