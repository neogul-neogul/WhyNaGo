package com.neogul.whynago.recommendation.domain;

import com.neogul.whynago.question.domain.Category;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// 카테고리 레이어와 태그 레이어를 2계층으로 둔 약점 프로필이다.
// 카테고리는 표본이 항상 충분해 큰 방향을 잡고, 태그는 표본이 2건 이상일 때만 신뢰한다.
public record WeaknessProfile(
        Map<Category, Double> categoryScores,
        List<TagWeakness> tagWeaknesses,
        int solvedCount
) {

    // 이력이 이보다 적으면 어떤 태그도 신뢰할 표본이 되지 못해 프로필 자체가 무의미하다.
    private static final int MIN_HISTORY_FOR_PROFILE = 3;

    public static WeaknessProfile empty() {
        return new WeaknessProfile(Map.of(), List.of(), 0);
    }

    public boolean isEmpty() {
        return solvedCount == 0;
    }

    // 약점을 진단할 만큼 풀지 않은 상태다. 맞춤 추천 대신 콜드스타트로 간다.
    public boolean isColdStart() {
        return solvedCount < MIN_HISTORY_FOR_PROFILE;
    }

    // 약점도 내림차순, 같으면 카테고리 ordinal 오름차순. 같은 프로필이면 항상 같은 순서가 나와야 한다.
    public List<Category> weakestCategories(int limit) {
        if (limit <= 0) {
            return List.of();
        }
        return categoryScores.entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<Category, Double>>comparingDouble(entry -> -entry.getValue())
                        .thenComparing(entry -> entry.getKey().ordinal()))
                .limit(limit)
                .map(Map.Entry::getKey)
                .toList();
    }
}
