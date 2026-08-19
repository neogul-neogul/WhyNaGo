package com.neogul.whynago.mastery.implement;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.domain.MasteryRecord;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.mastery.domain.UserTagMastery;
import com.neogul.whynago.mastery.infra.MasteryRecordRepository;
import com.neogul.whynago.mastery.infra.UserTagMasteryRepository;
import com.neogul.whynago.mastery.infra.dto.CategoryMasteryCount;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MasteryReader {

    private final MasteryRecordRepository masteryRecordRepository;
    private final UserTagMasteryRepository userTagMasteryRepository;

    public List<UserTagMastery> readTagMastery(Long userId) {
        return userTagMasteryRepository.findByUserId(userId);
    }

    public List<CategoryMasteryCount> readCategoryCounts(Long userId) {
        return masteryRecordRepository.countByCategoryAndLevel(userId);
    }

    // 추천의 약점 프로필이 쓴다. 같은 문항을 여러 번 풀었으면 최신 판정만 남긴다.
    // AI 판정만 고른다. 객관식의 RULE_CHOICE 판정도 같은 테이블에 쌓이지만 여기서 돌려주지 않는다 —
    // 문항 평균 소요시간이 매일 재집계되므로, 푼 시점의 기준이 아니라 현재 기준으로 다시 판정해야
    // 프로필이 최신 표본을 반영한다. 저장된 규칙 판정은 GET /api/mastery의 사용자 노출용이다.
    public Map<Long, MasteryLevel> readLatestAiLevelsByQuestion(Long userId, List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return masteryRecordRepository
                .findByUserIdAndSourceAndQuestionIdInOrderByIdAsc(userId, MasterySource.AI_ESSAY, questionIds)
                .stream()
                .collect(Collectors.toMap(
                        MasteryRecord::getQuestionId,
                        MasteryRecord::getLevel,
                        // 조회를 id 오름차순으로 했으므로 나중에 오는 값이 최신이다.
                        (older, newer) -> newer
                ));
    }
}
