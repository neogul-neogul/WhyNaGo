package com.neogul.whynago.question.infra.ai.promptevaluation;

import com.neogul.whynago.question.infra.ai.GeminiEssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;

final class PromptEvaluationRunner {

    private static final int MAX_ATTEMPTS = 3;

    // 생성이 실패한 조합의 자리를 채우는 값이다. 리포트에서 다른 버전과 나란히 비어 있는 칸으로 보인다.
    private static final GradeAndFollowupResult EMPTY_RESPONSE =
            new GradeAndFollowupResult(null, null, 0, null, null, null, List.of());
    private static final PromptEvaluationJudge.JudgeResult NOT_JUDGED =
            new PromptEvaluationJudge.JudgeResult(0, 0, 0, 0, 0, 0, 0, 0, "응답을 받지 못해 심사하지 않았다.");

    private final ChatClient.Builder chatClientBuilder;
    private final PromptEvaluationJudge judge;

    PromptEvaluationRunner(ChatClient.Builder chatClientBuilder, PromptEvaluationJudge judge) {
        this.chatClientBuilder = chatClientBuilder;
        this.judge = judge;
    }

    PromptEvaluationRun run(PromptEvaluationCase testCase, EssayPrompt prompt) {
        GradeAndFollowupResult response;
        try {
            response = attempt(() -> generate(testCase, prompt));
        } catch (RuntimeException e) {
            // 한 조합이 실패해도 나머지 비교 결과까지 잃지 않는다. 실패로 기록하고 리포트에 남긴다.
            return failedRun(testCase, prompt, EMPTY_RESPONSE, "응답 생성 실패: " + rootMessage(e));
        }

        PromptEvaluationJudge.JudgeResult judgeResult;
        try {
            judgeResult = attempt(() -> judge.judge(testCase, response));
        } catch (RuntimeException e) {
            return failedRun(testCase, prompt, response, "심사 실패: " + rootMessage(e));
        }
        return new PromptEvaluationRun(testCase, prompt, response, judgeResult, contractFailures(testCase, response, judgeResult));
    }

    private GradeAndFollowupResult generate(PromptEvaluationCase testCase, EssayPrompt prompt) {
        // 프롬프트·케이스마다 메모리를 새로 만들어 이전 비교 결과가 다음 호출에 섞이지 않게 한다.
        GeminiEssayAiClient client = new GeminiEssayAiClient(
                chatClientBuilder, MessageWindowChatMemory.builder().build(), prompt);
        return client.gradeAndGenerateFollowup(
                "prompt-evaluation-" + UUID.randomUUID(),
                testCase.target(),
                testCase.generateFollowup(),
                testCase.mode());
    }

    // 작은 로컬 모델은 같은 프롬프트에도 이따금 형식이 깨진 응답을 낸다. 프롬프트 품질과 무관한
    // 일회성 실패로 비교가 통째로 날아가지 않게 몇 번 다시 부른다.
    private <T> T attempt(Supplier<T> call) {
        RuntimeException last = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return call.get();
            } catch (RuntimeException e) {
                last = e;
            }
        }
        throw last;
    }

    private PromptEvaluationRun failedRun(
            PromptEvaluationCase testCase,
            EssayPrompt prompt,
            GradeAndFollowupResult response,
            String failure
    ) {
        return new PromptEvaluationRun(testCase, prompt, response, NOT_JUDGED, List.of(failure));
    }

    private String rootMessage(Throwable e) {
        Throwable root = e;
        while (root.getCause() != null) {
            root = root.getCause();
        }
        return root.getClass().getSimpleName() + ": " + root.getMessage();
    }

    private List<String> contractFailures(
            PromptEvaluationCase testCase,
            GradeAndFollowupResult response,
            PromptEvaluationJudge.JudgeResult judgeResult
    ) {
        List<String> failures = new ArrayList<>();
        if (isBlank(response.feedback())) {
            failures.add("feedback 누락");
        }
        if (isBlank(response.modelAnswer())) {
            failures.add("modelAnswer 누락");
        }
        if (response.score() < 0 || response.score() > 10) {
            failures.add("score 범위 이탈");
        }
        if (testCase.generateFollowup() && isBlank(response.followupQuestion())) {
            failures.add("followupQuestion 누락");
        }
        if (!testCase.generateFollowup() && response.followupQuestion() != null) {
            failures.add("followupQuestion이 null이 아님");
        }
        if (response.mastery() == null || isBlank(response.masteryReason())) {
            failures.add("mastery 또는 masteryReason 누락");
        }
        if (testCase.target().hasRubric()) {
            Set<Integer> metIndexes = response.criteriaResults().stream()
                    .filter(criterion -> criterion.met())
                    .map(criterion -> criterion.index())
                    .collect(java.util.stream.Collectors.toSet());
            Set<Integer> indexes = response.criteriaResults().stream()
                    .map(criterion -> criterion.index())
                    .collect(java.util.stream.Collectors.toSet());
            Set<Integer> expectedIndexes = java.util.stream.IntStream.rangeClosed(1, testCase.target().rubric().size())
                    .boxed()
                    .collect(java.util.stream.Collectors.toSet());
            if (!indexes.equals(expectedIndexes)) {
                failures.add("criteriaResults 항목 번호 불일치");
            }
            if (response.score() != testCase.target().rubric().scoreOf(metIndexes)) {
                failures.add("score와 criteriaResults 배점 합 불일치");
            }
        } else if (!response.criteriaResults().isEmpty()) {
            failures.add("루브릭 없는 응답의 criteriaResults가 비어 있지 않음");
        }
        if (response.followupQuestion() != null && testCase.target().rubric() != null
                && testCase.target().rubric().hasFollowupScope()) {
            testCase.target().rubric().followupScope().forbidden().stream()
                    .filter(forbidden -> response.followupQuestion().contains(forbidden))
                    .forEach(forbidden -> failures.add("꼬리질문 금지 범위 포함: " + forbidden));
        }
        if (!judgeResult.hasValidScores()) {
            failures.add("심사 점수 범위 이탈");
        }
        return List.copyOf(failures);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
