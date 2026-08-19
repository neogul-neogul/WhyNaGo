package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.infra.QuestionStatRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionStatReader {

    private final QuestionStatRepository questionStatRepository;

    // 통계가 없으면 비어 있다. 호출자가 표본 없음으로 다루게 하려고 기본값을 채우지 않는다.
    public Optional<QuestionStat> read(Long questionId) {
        return questionStatRepository.findById(questionId);
    }

    // 통계가 없는 문항은 맵에서 빠진다. 호출자가 표본 없음으로 다루게 하려고 기본값을 채우지 않는다.
    public Map<Long, QuestionStat> readAll(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return questionStatRepository.findAllById(questionIds).stream()
                .collect(Collectors.toMap(QuestionStat::getQuestionId, Function.identity()));
    }
}
