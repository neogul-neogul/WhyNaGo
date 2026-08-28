package com.neogul.whynago.question.infra.ai.compare;

import com.neogul.whynago.question.infra.ai.EssayAiClient;
import com.neogul.whynago.question.infra.ai.GeminiEssayAiClient;
import com.neogul.whynago.question.infra.ai.GradeAndFollowupResult;
import com.neogul.whynago.question.infra.ai.prompt.EssayPrompt;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestClient;

/**
 * 모델 하나를 실제로 호출하는 실행기. 운영과 같은 GeminiEssayAiClient·EssayPrompt를 그대로 태워
 * 모델만 바꿨을 때 응답이 어떻게 달라지는지 본다. 대화 메모리는 모델마다 새로 만들어 서로 섞이지 않게 한다.
 * 접속 정보는 설정에서 오므로 Gemini든 로컬 ollama든 같은 경로로 호출한다.
 */
public class EssayAiModelRunner implements AutoCloseable {

    private static final Duration RETRY_BACKOFF = Duration.ofSeconds(2);

    private final String model;
    private final EssayAiClient client;
    private final AiCallLogCaptor logCaptor;

    private EssayAiModelRunner(String model, EssayAiClient client, AiCallLogCaptor logCaptor) {
        this.model = model;
        this.client = client;
        this.logCaptor = logCaptor;
    }

    public static EssayAiModelRunner of(String model, EssayAiComparisonConfig config, EssayPrompt prompt) {
        ChatMemory chatMemory = MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .build();
        ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel(model, config));
        return new EssayAiModelRunner(model, client(config, chatClientBuilder, chatMemory, prompt), new AiCallLogCaptor());
    }

    // 기본은 Flux로 받아 조각을 바로 콘솔에 흘리는 쪽이고,
    // -Dai.compare.stream=false 면 운영과 똑같이 다 받은 뒤 한 번에 객체로 바꾸는 클라이언트를 태운다.
    private static EssayAiClient client(
            EssayAiComparisonConfig config,
            ChatClient.Builder chatClientBuilder,
            ChatMemory chatMemory,
            EssayPrompt prompt
    ) {
        if (config.streaming()) {
            return new StreamingEssayAiClient(chatClientBuilder, chatMemory, prompt);
        }
        return new GeminiEssayAiClient(chatClientBuilder, chatMemory, prompt);
    }

    public ModelScenarioResult run(EssayComparisonScenario scenario, int attempt) {
        String conversationId = conversationId(scenario, attempt);
        List<EssayTurnResponse> turns = new ArrayList<>();
        String question = scenario.question();

        try {
            for (int turnIndex = 0; turnIndex < scenario.turnCount(); turnIndex++) {
                EssayTurnResponse turn = callTurn(conversationId, scenario, turnIndex, question);
                turns.add(turn);

                question = turn.result().followupQuestion();
                // 꼬리질문이 없으면 이어갈 질문이 없으므로 남은 턴은 건너뛴다.
                if (question == null || question.isBlank()) {
                    break;
                }
            }
            return ModelScenarioResult.completed(model, attempt, scenario, turns);
        } catch (RuntimeException e) {
            return ModelScenarioResult.failed(model, attempt, scenario, turns, e);
        } finally {
            client.clearSession(conversationId);
        }
    }

    @Override
    public void close() {
        logCaptor.close();
    }

    private EssayTurnResponse callTurn(
            String conversationId,
            EssayComparisonScenario scenario,
            int turnIndex,
            String question
    ) {
        String answer = scenario.answers().get(turnIndex);
        boolean generateFollowup = scenario.generateFollowupAt(turnIndex);

        logCaptor.reset();
        long startedAt = System.nanoTime();
        GradeAndFollowupResult result = client.gradeAndGenerateFollowup(
                conversationId, question, answer, generateFollowup, scenario.mode());
        long elapsedMs = Duration.ofNanos(System.nanoTime() - startedAt).toMillis();

        return new EssayTurnResponse(
                turnIndex + 1,
                question,
                answer,
                generateFollowup,
                result,
                elapsedMs,
                logCaptor.lastMetrics().orElse(null));
    }

    private String conversationId(EssayComparisonScenario scenario, int attempt) {
        return "%s-%s-%d".formatted(model, scenario.name().hashCode(), attempt);
    }

    private static OpenAiChatModel chatModel(String model, EssayAiComparisonConfig config) {
        OpenAiApi openAiApi = OpenAiApi.builder()
                .baseUrl(config.baseUrl())
                .apiKey(config.apiKey())
                .completionsPath(config.completionsPath())
                .restClientBuilder(RestClient.builder().requestFactory(requestFactory(config)))
                .build();

        OpenAiChatOptions.Builder options = OpenAiChatOptions.builder()
                .model(model)
                .temperature(config.temperature())
                // 스트리밍은 요청하지 않으면 토큰 사용량을 안 준다. 비교 리포트의 토큰 열이 비지 않게 켠다.
                .streamUsage(config.streaming());
        if (config.hasReasoningEffort()) {
            options.reasoningEffort(config.reasoningEffort());
        }

        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options.build())
                .retryTemplate(retryTemplate(config))
                .build();
    }

    // 로컬 모델은 한 번 답하는 데 수십 초가 걸리고 첫 호출에는 모델 적재 시간까지 더해진다.
    // 기본 읽기 타임아웃으로는 로컬 비교가 전부 실패하므로 설정값으로 늘린다.
    private static ClientHttpRequestFactory requestFactory(EssayAiComparisonConfig config) {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory();
        requestFactory.setReadTimeout(config.timeout());
        return requestFactory;
    }

    // Spring AI 기본 재시도는 10회라, 모델 하나가 계속 실패하면 비교 전체가 하염없이 늘어진다.
    // 비교 테스트는 실패도 결과로 기록하므로 재시도를 줄여 빨리 다음 모델로 넘어간다.
    private static RetryTemplate retryTemplate(EssayAiComparisonConfig config) {
        return RetryTemplate.builder()
                .maxAttempts(config.maxAttempts())
                .fixedBackoff(RETRY_BACKOFF)
                .build();
    }
}
