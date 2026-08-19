package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.implement.dto.QuestionStatSnapshot;
import com.neogul.whynago.question.infra.QuestionStatRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionStatWriter {

    private final QuestionStatRepository questionStatRepository;

    // PK가 questionId라 신규·기존이 섞인다. 기존 행은 갱신하고 없는 행만 새로 만든다.
    public void upsertAll(List<QuestionStatSnapshot> snapshots, LocalDateTime updatedAt) {
        if (snapshots.isEmpty()) {
            return;
        }
        Map<Long, QuestionStat> stored = questionStatRepository
                .findAllById(snapshots.stream().map(QuestionStatSnapshot::questionId).toList())
                .stream()
                .collect(Collectors.toMap(QuestionStat::getQuestionId, Function.identity()));

        List<QuestionStat> toSave = snapshots.stream()
                .map(snapshot -> merge(stored.get(snapshot.questionId()), snapshot, updatedAt))
                .toList();

        questionStatRepository.saveAll(toSave);
    }

    private QuestionStat merge(QuestionStat stored, QuestionStatSnapshot snapshot, LocalDateTime updatedAt) {
        if (stored == null) {
            return QuestionStat.of(
                    snapshot.questionId(),
                    snapshot.avgElapsedSeconds(),
                    snapshot.correctRate(),
                    snapshot.sampleCount(),
                    updatedAt
            );
        }
        stored.refresh(snapshot.avgElapsedSeconds(), snapshot.correctRate(), snapshot.sampleCount(), updatedAt);
        return stored;
    }
}
