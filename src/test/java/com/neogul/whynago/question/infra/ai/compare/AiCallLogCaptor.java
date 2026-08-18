package com.neogul.whynago.question.infra.ai.compare;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.neogul.whynago.question.infra.ai.GeminiEssayAiClient;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.LoggerFactory;

/**
 * 토큰 사용량은 EssayAiClient의 반환값이 아니라 호출 완료 로그에만 남는다.
 * 비교 리포트에 모델별 비용을 적기 위해, 운영 코드를 건드리지 않고 그 로그를 읽어 온다.
 */
public class AiCallLogCaptor implements AutoCloseable {

    private static final Pattern MODEL = Pattern.compile("model=([^,]+)");
    private static final Pattern PROMPT_TOKENS = Pattern.compile("promptTokens=(\\d+)");
    private static final Pattern COMPLETION_TOKENS = Pattern.compile("completionTokens=(\\d+)");
    private static final Pattern TOTAL_TOKENS = Pattern.compile("totalTokens=(\\d+)");

    private final ListAppender<ILoggingEvent> appender = new ListAppender<>();

    public AiCallLogCaptor() {
        appender.start();
        clientLogger().addAppender(appender);
    }

    public void reset() {
        appender.list.clear();
    }

    public Optional<AiCallMetrics> lastMetrics() {
        return appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .filter(message -> message.contains("totalTokens="))
                .reduce((first, second) -> second)
                .map(AiCallLogCaptor::parse);
    }

    @Override
    public void close() {
        clientLogger().detachAppender(appender);
        appender.stop();
    }

    private static AiCallMetrics parse(String message) {
        return new AiCallMetrics(
                text(MODEL, message),
                number(PROMPT_TOKENS, message),
                number(COMPLETION_TOKENS, message),
                number(TOTAL_TOKENS, message));
    }

    private static String text(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? matcher.group(1) : "unknown";
    }

    private static long number(Pattern pattern, String message) {
        Matcher matcher = pattern.matcher(message);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : 0L;
    }

    private static Logger clientLogger() {
        return (Logger) LoggerFactory.getLogger(GeminiEssayAiClient.class);
    }
}
