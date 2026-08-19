package com.neogul.whynago.recommendation.domain;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import java.util.Comparator;
import java.util.List;

// 약점 프로필을 생성 요청 단위(GenerationTopic)로 바꾼다. 같은 프로필이면 항상 같은 목록이 나와야 하므로
// 정렬 기준을 끝까지 고정한다.
public class RecommendationTopicPolicy {

    // 카테고리 상위 3개까지만 본다. 더 넓히면 약하지 않은 주제까지 생성 대상이 된다.
    private static final int MAX_CATEGORIES = 3;
    // 한 카테고리에 붙일 태그 수 상한. 생성 문항의 태그 상한(1~2개)과 같다.
    private static final int MAX_TAGS_PER_CATEGORY = 2;

    private static final double LOW_DIFFICULTY_THRESHOLD = 0.7;
    private static final double MEDIUM_DIFFICULTY_THRESHOLD = 0.35;

    public List<GenerationTopic> select(WeaknessProfile profile, int limit) {
        if (profile.isEmpty() || limit <= 0) {
            return List.of();
        }

        return profile.weakestCategories(Math.min(MAX_CATEGORIES, limit)).stream()
                .map(category -> toTopic(category, profile.categoryScores().get(category), profile))
                .toList();
    }

    private GenerationTopic toTopic(Category category, double weaknessScore, WeaknessProfile profile) {
        List<String> tags = trustedTags(category, profile);
        return new GenerationTopic(
                category,
                tags,
                targetDifficulty(weaknessScore),
                weaknessScore,
                reason(category, weaknessScore, tags)
        );
    }

    // 신뢰 표본에 못 미치는 태그는 붙이지 않는다. 붙일 태그가 없으면 카테고리만으로 생성한다.
    private List<String> trustedTags(Category category, WeaknessProfile profile) {
        return profile.tagWeaknesses().stream()
                .filter(tag -> tag.category() == category)
                .filter(TagWeakness::trusted)
                .sorted(Comparator
                        .comparingDouble((TagWeakness tag) -> -tag.weaknessScore())
                        .thenComparing(TagWeakness::name))
                .limit(MAX_TAGS_PER_CATEGORY)
                .map(TagWeakness::name)
                .toList();
    }

    // 개념이 안 잡힌 주제에 상 난이도를 주지 않는다. 약점이 클수록 쉬운 문항을 낸다.
    private Difficulty targetDifficulty(double weaknessScore) {
        if (weaknessScore >= LOW_DIFFICULTY_THRESHOLD) {
            return Difficulty.LOW;
        }
        if (weaknessScore >= MEDIUM_DIFFICULTY_THRESHOLD) {
            return Difficulty.MEDIUM;
        }
        return Difficulty.HIGH;
    }

    private String reason(Category category, double weaknessScore, List<String> tags) {
        String rounded = String.format("%.2f", weaknessScore);
        if (tags.isEmpty()) {
            return "%s 카테고리 약점도 %s, 신뢰할 태그 표본 없음".formatted(category.name(), rounded);
        }
        return "%s 카테고리 약점도 %s, 취약 태그: %s".formatted(category.name(), rounded, String.join(", ", tags));
    }
}
