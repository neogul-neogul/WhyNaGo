package com.neogul.whynago.mastery.service.dto;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.question.domain.Category;
import java.util.List;

// mastery 도메인은 question 저장소를 참조하지 않는다. 판정을 태그·카테고리에 연결하는 데 필요한 값은
// 호출자(채점 흐름)가 채워 넘긴다. tagIds가 비면 카테고리 단위 신호로만 기록된다.
public record RecordMasteryCommand(
        Long userId,
        Long questionId,
        Category category,
        List<Long> tagIds,
        MasteryLevel level,
        String reason,
        MasterySource source
) {
}
