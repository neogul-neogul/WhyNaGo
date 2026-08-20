package com.neogul.whynago.mastery.implement;

import com.neogul.whynago.mastery.domain.MasteryRecord;
import com.neogul.whynago.mastery.domain.UserTagMastery;
import com.neogul.whynago.mastery.infra.MasteryRecordRepository;
import com.neogul.whynago.mastery.infra.UserTagMasteryRepository;
import com.neogul.whynago.mastery.service.dto.RecordMasteryCommand;
import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 판정 1건을 이력에 쌓고 태그별 현재값을 갱신한다.
//
// 꼬리질문 판정은 이력에만 쌓고 현재값은 건드리지 않는다. 꼬리질문은 본질문보다 깊게 파고드는
// 프로브라 판정이 더 낮게 나오는 것이 정상인데, 현재값을 덮어쓰면 마지막(가장 깊은) 프로브가
// 그 태그의 숙련도가 되어 본질문을 제대로 답한 사용자가 그 주제를 모르는 것으로 기록된다.
@Component
@RequiredArgsConstructor
public class MasteryRecordAppender {

    private final MasteryRecordRepository masteryRecordRepository;
    private final UserTagMasteryRepository userTagMasteryRepository;
    private final Clock clock;

    public void append(RecordMasteryCommand command) {
        LocalDateTime now = LocalDateTime.now(clock);

        if (command.tagIds().isEmpty()) {
            // 태그가 없는 문항도 카테고리 신호는 남긴다. 이 판정은 태그별 현재값에는 반영되지 않는다.
            masteryRecordRepository.save(record(command, null, now));
            return;
        }

        masteryRecordRepository.saveAll(command.tagIds().stream()
                .map(tagId -> record(command, tagId, now))
                .toList());

        if (command.source().isFollowup()) {
            return;
        }
        userTagMasteryRepository.saveAll(command.tagIds().stream()
                .map(tagId -> upsert(command, tagId, now))
                .toList());
    }

    private MasteryRecord record(RecordMasteryCommand command, Long tagId, LocalDateTime now) {
        return MasteryRecord.of(
                command.userId(),
                command.questionId(),
                tagId,
                command.category(),
                command.level(),
                command.reason(),
                command.source(),
                command.turn(),
                now
        );
    }

    // 같은 (user, tag)는 행이 하나다. 누적이 아니라 최신 판정으로 덮어쓴다.
    private UserTagMastery upsert(RecordMasteryCommand command, Long tagId, LocalDateTime now) {
        return userTagMasteryRepository.findByUserIdAndTagId(command.userId(), tagId)
                .map(stored -> {
                    stored.refresh(command.level(), command.reason(), now);
                    return stored;
                })
                .orElseGet(() -> UserTagMastery.of(
                        command.userId(), tagId, command.level(), command.reason(), now));
    }
}
