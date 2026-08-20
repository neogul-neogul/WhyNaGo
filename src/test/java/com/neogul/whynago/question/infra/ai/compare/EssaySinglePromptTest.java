package com.neogul.whynago.question.infra.ai.compare;

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.neogul.whynago.question.domain.EssayGradingMode;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

/**
 * 프롬프트를 모델 하나에 딱 한 번만 던져 응답을 눈으로 확인하는 테스트다.
 * 여러 모델·여러 시도를 돌리는 EssayAiModelComparisonTest와 달리 호출이 한 번뿐이라,
 * 프롬프트를 고치고 결과를 빠르게 확인할 때 쓴다. 모델은 설정의 첫 번째 모델을 쓴다.
 *
 * <pre>
 * # 로컬 ollama의 첫 모델로 기본 문답 한 번
 * ./gradlew test --tests "*EssaySinglePromptTest" -Dai.compare=true -Dai.compare.config=ollama
 *
 * # 모델·프롬프트 버전 지정
 * ./gradlew test --tests "*EssaySinglePromptTest" -Dai.compare=true -Dai.compare.config=ollama \
 *   -Dai.compare.models=mistral:latest -Dai.compare.promptVersion=v2
 *
 * # 질문·답변을 직접 넣어서 확인
 * ./gradlew test --tests "*EssaySinglePromptTest" -Dai.compare=true -Dai.compare.config=ollama \
 *   -Dai.compare.question="깊은 복사와 얕은 복사의 차이는?" \
 *   -Dai.compare.answer="얕은 복사는 주소만 복사하고 깊은 복사는 값을 전부 복사합니다." \
 *   -Dai.compare.mode=INTERVIEW -Dai.compare.followup=false
 * </pre>
 *
 * 응답은 콘솔에 그대로 찍고, 비교 리포트 파일은 건드리지 않는다.
 */
@EnabledIfSystemProperty(named = "ai.compare", matches = "true")
class EssaySinglePromptTest {

    private static final String PROPERTY_PREFIX = "ai.compare.";
    private static final Duration PROBE_TIMEOUT = Duration.ofSeconds(2);
    private static final String SCENARIO_NAME = "단일 호출";

    private static final EssayAiComparisonConfig CONFIG = EssayAiComparisonConfig.load();

    @BeforeAll
    static void requireReachableEndpoint() {
        assumeTrue(CONFIG.hasApiKey(),
                "API 키가 없어 단일 호출을 건너뛴다. API_KEY 환경변수나 -Dai.compare.apiKey 로 전달한다.");
        assumeTrue(AiEndpointProbe.isReachable(CONFIG.baseUrl(), PROBE_TIMEOUT),
                "%s 에 연결할 수 없어 단일 호출을 건너뛴다. 로컬 ollama라면 `ollama serve` 로 먼저 띄운다."
                        .formatted(CONFIG.baseUrl()));
    }

    @Test
    @DisplayName("프롬프트를 한 번만 호출해 응답을 확인한다.")
    void callOnce() {
        // given
        String model = CONFIG.models().getFirst();
        EssayComparisonScenario scenario = scenario();
        SoftAssertions softly = new SoftAssertions();

        // when
        ModelScenarioResult result;
        try (EssayAiModelRunner runner =
                     EssayAiModelRunner.of(model, CONFIG, EssayPromptVersions.of(CONFIG.promptVersion()))) {
            result = runner.run(scenario, 1);
        }

        // then
        print(result);
        EssayResponseContract.assertSatisfied(softly, result);
        softly.assertAll();
    }

    // 질문·답변을 주지 않으면 비교용 시나리오 하나를 그대로 한 번만 돌린다.
    private EssayComparisonScenario scenario() {
        EssayComparisonScenario base = EssayComparisonScenarioFixture.partiallyWrongAnswer();
        return new EssayComparisonScenario(
                SCENARIO_NAME,
                mode(base.mode()),
                property("question", base.question()),
                List.of(property("answer", base.answers().getFirst())),
                Boolean.parseBoolean(property("followup", "true")));
    }

    private EssayGradingMode mode(EssayGradingMode defaultMode) {
        return EssayGradingMode.valueOf(property("mode", defaultMode.name()).toUpperCase(Locale.ROOT));
    }

    private void print(ModelScenarioResult result) {
        System.out.printf("%n=== 단일 호출: %s (설정 %s, 프롬프트 %s, %s) ===%n",
                result.model(), CONFIG.displayName(), CONFIG.promptVersion(), result.scenario().mode());

        if (!result.succeeded()) {
            System.out.println("호출 실패: " + result.failure());
            return;
        }

        EssayTurnResponse turn = result.turns().getFirst();
        System.out.printf("소요 %dms, 토큰 %s%n%n", turn.elapsedMs(), tokens(turn));
        System.out.println("[질문]\n" + turn.question());
        System.out.println("\n[답변]\n" + turn.answer());
        System.out.println("\n[score] " + turn.result().score());
        System.out.println("\n[feedback]\n" + turn.result().feedback());
        System.out.println("\n[modelAnswer]\n" + turn.result().modelAnswer());
        System.out.printf("%n[followupQuestion]%s%n%s%n%n",
                turn.followupRequested() ? "" : " (요청 안 함)", turn.result().followupQuestion());
    }

    private String tokens(EssayTurnResponse turn) {
        AiCallMetrics metrics = turn.metrics();
        if (metrics == null) {
            return "-";
        }
        return "%d/%d".formatted(metrics.promptTokens(), metrics.completionTokens());
    }

    private static String property(String name, String defaultValue) {
        String value = System.getProperty(PROPERTY_PREFIX + name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
