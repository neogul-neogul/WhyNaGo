package com.neogul.whynago.problemset.implement;

import com.neogul.whynago.problemset.domain.ProblemSetItem;
import com.neogul.whynago.problemset.infra.ProblemSetItemRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProblemSetItemReader {

    private final ProblemSetItemRepository problemSetItemRepository;

    public List<ProblemSetItem> readAll(Long problemSetId) {
        return problemSetItemRepository.findByProblemSetIdOrderByIdAsc(problemSetId);
    }

    public Map<Long, List<ProblemSetItem>> readAllGroupedByProblemSetId(List<Long> problemSetIds) {
        if (problemSetIds.isEmpty()) {
            return Map.of();
        }
        return problemSetItemRepository.findByProblemSetIdInOrderByIdAsc(problemSetIds).stream()
                .collect(Collectors.groupingBy(ProblemSetItem::getProblemSetId));
    }
}
