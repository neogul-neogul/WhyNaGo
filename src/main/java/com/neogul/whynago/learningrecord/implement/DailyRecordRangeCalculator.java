package com.neogul.whynago.learningrecord.implement;

import com.neogul.whynago.learningrecord.implement.dto.DateTimeRange;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DailyRecordRangeCalculator {

    private static final int DEFAULT_RANGE_DAYS = 364;

    public Optional<DateTimeRange> resolve(LocalDate from, LocalDate to, LocalDate today) {
        LocalDate resolvedTo = to != null ? to : today;
        LocalDate resolvedFrom = from != null ? from : resolvedTo.minusDays(DEFAULT_RANGE_DAYS);
        if (resolvedFrom.isAfter(resolvedTo)) {
            return Optional.empty();
        }

        return Optional.of(new DateTimeRange(resolvedFrom.atStartOfDay(), resolvedTo.atTime(LocalTime.MAX)));
    }
}
