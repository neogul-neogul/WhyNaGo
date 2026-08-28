package com.neogul.whynago.question.infra.ai.compare;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * 같은 프롬프트를 여러 AI 모델에 던져 응답이 얼마나 다른지 눈으로 확인하는 탐색용 테스트다.
 * 실제 API를 호출해 비용·쿼터를 쓰므로 기본 빌드에서는 실행되지 않고, -Dai.compare=true 로만 켜진다.
 *
 * <pre>
 * # 기본 모델 3종 비교 (Gemini, API_KEY 환경변수 필요)
 * ./gradlew test --tests "*EssayAiModelComparisonTest" -Dai.compare=true
 *
 * # 로컬 ollama 모델 비교 (src/test/resources/ai-compare/ollama.yml)
 * ./gradlew test --tests "*EssayAiModelComparisonTest" -Dai.compare=true -Dai.compare.config=ollama
 *
 * # 비교 대상·옵션 지정
 * ./gradlew test --tests "*EssayAiModelComparisonTest" -Dai.compare=true \
 *   -Dai.compare.models=gemini-3.5-flash-lite,gemini-2.5-flash \
 *   -Dai.compare.promptVersion=v3 -Dai.compare.temperature=0.3 -Dai.compare.repeat=3
 * </pre>
 *
 * 결과는 콘솔과 build/reports/ai-model-comparison/essay-model-comparison.md 에 함께 남는다.
 */
@EnabledIfSystemProperty(named = "ai.compare", matches = "true")
class EssayAiModelComparisonTest {

    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(2);

    private static final EssayAiComparisonConfig CONFIG = EssayAiComparisonConfig.load();
    private static final EssayComparisonReporter REPORTER = new EssayComparisonReporter(CONFIG);

    @BeforeAll
    static void requireReachableEndpoint() {
        assumeTrue(CONFIG.hasApiKey(),
                "API 키가 없어 모델 비교를 건너뛴다. API_KEY 환경변수나 -Dai.compare.apiKey 로 전달한다.");
        assumeTrue(AiEndpointProbe.isReachable(CONFIG.baseUrl(), PROBE_TIMEOUT),
                "%s 에 연결할 수 없어 모델 비교를 건너뛴다. 로컬 ollama라면 `ollama serve` 로 먼저 띄운다."
                        .formatted(CONFIG.baseUrl()));
    }

    @AfterAll
    static void writeReport() {
        if (REPORTER.isEmpty()) {
            return;
        }
        Path report = REPORTER.write();
        System.out.println("모델 비교 리포트: " + report);
    }

    @Test
    @DisplayName("같은 프롬프트에 대한 모델별 응답 차이를 비교한다.")
    void compareModels() {
        // given
        List<EssayComparisonScenario> scenarios = EssayComparisonScenarioFixture.scenarios();
        SoftAssertions softly = new SoftAssertions();

        // when
        for (String model : CONFIG.models()) {
            try (EssayAiModelRunner runner = runnerFor(model)) {
                scenarios.stream()
                        .map(scenario -> runner.run(scenario, 1))
                        .forEach(result -> {
                            REPORTER.add(result);
                            EssayResponseContract.assertSatisfied(softly, result);
                        });
            }
        }

        // then
        softly.assertAll();
    }

    @Test
    @DisplayName("같은 모델에 같은 질문을 반복하면 응답이 얼마나 흔들리는지 비교한다.")
    void compareRepeatedCalls() {
        // given
        EssayComparisonScenario scenario = EssayComparisonScenarioFixture.repeatedCalls();
        SoftAssertions softly = new SoftAssertions();

        // when
        for (String model : CONFIG.models()) {
            try (EssayAiModelRunner runner = runnerFor(model)) {
                for (int attempt = 1; attempt <= CONFIG.repeatCount(); attempt++) {
                    ModelScenarioResult result = runner.run(scenario, attempt);
                    REPORTER.add(result);
                    EssayResponseContract.assertSatisfied(softly, result);
                }
            }
        }

        // then
        softly.assertAll();
    }

    private EssayAiModelRunner runnerFor(String model) {
        return EssayAiModelRunner.of(model, CONFIG, EssayPromptVersions.of(CONFIG.promptVersion()));
    }
}
