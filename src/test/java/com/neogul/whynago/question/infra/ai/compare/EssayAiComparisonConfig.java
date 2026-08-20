package com.neogul.whynago.question.infra.ai.compare;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

/**
 * 모델 비교 실행 옵션.
 * 값은 (1) 코드 기본값(Gemini) → (2) -Dai.compare.config=&lt;이름&gt; 으로 고른 설정 파일
 * (src/test/resources/ai-compare/&lt;이름&gt;.yml) → (3) -Dai.compare.* 시스템 프로퍼티 순으로 덮어쓴다.
 * 뒤에 오는 것이 이긴다.
 * API 키는 코드에 두지 않고 설정 파일·시스템 프로퍼티·API_KEY 환경변수로만 받는다.
 */
public record EssayAiComparisonConfig(
        String name,
        List<String> models,
        String apiKey,
        String baseUrl,
        String completionsPath,
        double temperature,
        String reasoningEffort,
        String promptVersion,
        int repeatCount,
        Duration timeout,
        int maxAttempts,
        boolean streaming
) {

    /** 설정 파일을 고르지 않았을 때 쓰는 이름. 코드 기본값(Gemini)으로 돈다. */
    public static final String DEFAULT_CONFIG = "";

    private static final String PROPERTY_PREFIX = "ai.compare.";
    private static final String CONFIG_LOCATION = "ai-compare/%s.yml";
    private static final String NO_REASONING_EFFORT = "none";

    private static final List<String> DEFAULT_MODELS =
            List.of("gemini-3.5-flash-lite", "gemini-2.5-flash-lite", "gemini-2.5-flash");
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta/openai";
    private static final String DEFAULT_COMPLETIONS_PATH = "/chat/completions";
    private static final double DEFAULT_TEMPERATURE = 0.3;
    private static final String DEFAULT_REASONING_EFFORT = "low";
    private static final String DEFAULT_PROMPT_VERSION = "v3";
    private static final int DEFAULT_REPEAT_COUNT = 1;
    private static final int DEFAULT_TIMEOUT_SECONDS = 120;
    private static final int DEFAULT_MAX_ATTEMPTS = 2;
    // 응답을 Flux로 받아 도착하는 대로 콘솔에 흘린다. -Dai.compare.stream=false 면 운영과 같은 블로킹 호출.
    private static final boolean DEFAULT_STREAMING = true;

    public static EssayAiComparisonConfig load() {
        String name = property(new Properties(), "config", DEFAULT_CONFIG);
        Properties file = readConfigFile(name);

        return new EssayAiComparisonConfig(
                name,
                resolveModels(file),
                resolveApiKey(file),
                property(file, "baseUrl", DEFAULT_BASE_URL),
                property(file, "completionsPath", DEFAULT_COMPLETIONS_PATH),
                Double.parseDouble(property(file, "temperature", String.valueOf(DEFAULT_TEMPERATURE))),
                property(file, "reasoningEffort", DEFAULT_REASONING_EFFORT),
                property(file, "promptVersion", DEFAULT_PROMPT_VERSION),
                Integer.parseInt(property(file, "repeat", String.valueOf(DEFAULT_REPEAT_COUNT))),
                Duration.ofSeconds(
                        Long.parseLong(property(file, "timeoutSeconds", String.valueOf(DEFAULT_TIMEOUT_SECONDS)))),
                Integer.parseInt(property(file, "maxAttempts", String.valueOf(DEFAULT_MAX_ATTEMPTS))),
                Boolean.parseBoolean(property(file, "stream", String.valueOf(DEFAULT_STREAMING)))
        );
    }

    public boolean hasApiKey() {
        return !apiKey.isBlank();
    }

    // 로컬 모델처럼 추론 강도 옵션이 없는 대상은 설정 파일에서 none으로 꺼 둔다.
    public boolean hasReasoningEffort() {
        return !reasoningEffort.isBlank() && !reasoningEffort.equalsIgnoreCase(NO_REASONING_EFFORT);
    }

    public String displayName() {
        return name.isBlank() ? "기본(Gemini)" : name;
    }

    // 설정 파일을 yml로 두는 이유: .properties는 IDE가 ISO-8859-1로 읽어 한글 주석이 깨진다.
    // ai.compare.* 를 계층으로 적어도 여기서 평평한 키로 펴서 시스템 프로퍼티와 같은 이름으로 다룬다.
    private static Properties readConfigFile(String name) {
        if (name.isBlank()) {
            return new Properties();
        }

        String location = CONFIG_LOCATION.formatted(name);
        ClassPathResource config = new ClassPathResource(location);
        if (!config.exists()) {
            throw new IllegalArgumentException("모델 비교 설정 파일을 찾을 수 없다: " + location);
        }

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(config);
        Properties properties = yaml.getObject();
        return properties == null ? new Properties() : properties;
    }

    private static List<String> resolveModels(Properties file) {
        String models = property(file, "models", "");
        if (models.isBlank()) {
            return DEFAULT_MODELS;
        }
        return Arrays.stream(models.split(","))
                .map(String::trim)
                .filter(model -> !model.isBlank())
                .toList();
    }

    // 환경변수는 Gemini 운영 키를 그대로 쓰는 통로라, 설정 파일이 키를 정해 둔 경우에는 설정 파일이 이긴다.
    private static String resolveApiKey(Properties file) {
        String configured = property(file, "apiKey", "");
        if (!configured.isBlank()) {
            return configured;
        }
        String fromEnvironment = System.getenv("API_KEY");
        return fromEnvironment == null ? "" : fromEnvironment.trim();
    }

    private static String property(Properties file, String name, String defaultValue) {
        String key = PROPERTY_PREFIX + name;
        String value = System.getProperty(key, file.getProperty(key));
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
