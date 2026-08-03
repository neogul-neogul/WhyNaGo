package com.neogul.whynago.learningrecord.implement;

import com.neogul.whynago.learningrecord.implement.dto.DailyCount;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class DailyRecordAggregator {

    public List<DailyCount> aggregate(List<SolvedSession> sessions) {
        Map<LocalDate, List<SolvedSession>> groupedByDate = sessions.stream()
                .collect(Collectors.groupingBy(session -> session.getSolvedAt().toLocalDate()));

        return groupedByDate.entrySet().stream()
                .map(entry -> new DailyCount(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream().mapToInt(SolvedSession::getTotalCount).sum()
                ))
                .sorted(Comparator.comparing(DailyCount::date))
                .toList();
    }
}