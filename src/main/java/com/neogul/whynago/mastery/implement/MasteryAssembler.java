package com.neogul.whynago.mastery.implement;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.domain.UserTagMastery;
import com.neogul.whynago.mastery.infra.dto.CategoryMasteryCount;
import com.neogul.whynago.mastery.service.dto.CategoryMasteryResult;
import com.neogul.whynago.mastery.service.dto.MasteryResult;
import com.neogul.whynago.mastery.service.dto.TagMasteryResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.implement.TagReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 태그별 현재값과 카테고리별 판정 분포를 하나의 조회 결과로 조립한다.
//
// 태그 이름은 question 도메인의 사전(tag)에 있으므로 여기서만 question을 읽는다.
// 기록 경로(MasteryRecordAppender)는 question을 참조하지 않으며, 필요한 tagId·category는 호출자가 넘긴다.
// 따라서 클래스 수준 의존은 한 방향으로만 흐른다(기록: question -> mastery, 조회: mastery -> question).
@Component
@RequiredArgsConstructor
public class MasteryAssembler {

    private final MasteryReader masteryReader;
    private final TagReader tagReader;

    public MasteryResult assemble(Long userId) {
        List<UserTagMastery> tagMastery = masteryReader.readTagMastery(userId);
        Map<Category, Map<MasteryLevel, Long>> levelCounts = groupLevelCounts(masteryReader.readCategoryCounts(userId));
        Map<Category, List<TagMasteryResult>> tagsByCategory = groupTags(tagMastery);

        List<CategoryMasteryResult> categories = new ArrayList<>();
        for (Category category : Category.values()) {
            Map<MasteryLevel, Long> counts = levelCounts.getOrDefault(category, Map.of());
            List<TagMasteryResult> tags = tagsByCategory.getOrDefault(category, List.of());
            // 판정이 하나도 없는 카테고리는 내려보내지 않는다. 빈 칸을 채우는 판단은 클라이언트가 한다.
            if (!counts.isEmpty() || !tags.isEmpty()) {
                categories.add(new CategoryMasteryResult(category, counts, tags));
            }
        }
        return new MasteryResult(categories);
    }

    private Map<Category, Map<MasteryLevel, Long>> groupLevelCounts(List<CategoryMasteryCount> counts) {
        Map<Category, Map<MasteryLevel, Long>> grouped = new EnumMap<>(Category.class);
        for (CategoryMasteryCount count : counts) {
            grouped.computeIfAbsent(count.getCategory(), category -> new EnumMap<>(MasteryLevel.class))
                    .put(MasteryLevel.valueOf(count.getLevel()), count.getCount());
        }
        return grouped;
    }

    private Map<Category, List<TagMasteryResult>> groupTags(List<UserTagMastery> tagMastery) {
        Map<Long, Tag> tags = tagReader.readByIds(tagMastery.stream()
                .map(UserTagMastery::getTagId)
                .toList());

        Map<Category, List<TagMasteryResult>> grouped = new LinkedHashMap<>();
        tagMastery.stream()
                // 최근에 판정된 태그가 먼저 보이게 한다.
                .sorted(Comparator.comparing(UserTagMastery::getUpdatedAt).reversed())
                .forEach(mastery -> {
                    Tag tag = tags.get(mastery.getTagId());
                    if (tag == null) {
                        return;
                    }
                    grouped.computeIfAbsent(tag.getCategory(), category -> new ArrayList<>())
                            .add(new TagMasteryResult(
                                    tag.getId(),
                                    tag.getName(),
                                    mastery.getLevel(),
                                    mastery.getReason(),
                                    mastery.getUpdatedAt()
                            ));
                });
        return grouped;
    }
}
