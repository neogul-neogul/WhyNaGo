package com.neogul.whynago.common.ai;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.ai.retry.NonTransientAiException;

// 실패 원인을 분류만 한다. 에러코드로 옮기는 일은 호출하는 도메인이 각자의 쿼터 버킷에 맞춰 처리한다.
public final class AiFailureClassifier {

    private static final String QUOTA_STATUS = "RESOURCE_EXHAUSTED";
    private static final Pattern QUOTA_STATUS_CODE = Pattern.compile("\\b429\\b");
    private static final List<String> DAILY_QUOTA_HINTS = List.of("perday", "per day", "daily");

    private AiFailureClassifier() {
    }

    public static AiFailureKind classify(RuntimeException exception) {
        String detail = exception.getMessage();
        if (!(exception instanceof NonTransientAiException) || !isQuotaExceeded(detail)) {
            return AiFailureKind.UNAVAILABLE;
        }
        return isDailyQuota(detail) ? AiFailureKind.DAILY_QUOTA_EXCEEDED : AiFailureKind.QUOTA_EXCEEDED;
    }

    private static boolean isQuotaExceeded(String detail) {
        if (detail == null) {
            return false;
        }
        return detail.contains(QUOTA_STATUS) || QUOTA_STATUS_CODE.matcher(detail).find();
    }

    private static boolean isDailyQuota(String detail) {
        String normalized = detail.toLowerCase(Locale.ROOT);
        return DAILY_QUOTA_HINTS.stream().anyMatch(normalized::contains);
    }
}
