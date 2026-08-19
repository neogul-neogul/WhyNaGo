package com.neogul.whynago.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RecommendationTopicPolicyTest {

    private static final int LIMIT = 3;

    private final RecommendationTopicPolicy policy = new RecommendationTopicPolicy();

    @Test
    @DisplayName("약점도가 높은 카테고리 상위 3개만 생성 대상으로 고른다.")
    void select_topCategories() {
        Map<Category, Double> scores = new LinkedHashMap<>();
        scores.put(Category.DB, 0.9);
        scores.put(Category.NETWORK, 0.8);
        scores.put(Category.OS, 0.7);
        scores.put(Category.ALGORITHM, 0.6);

        List<GenerationTopic> topics = policy.select(profile(scores, List.of()), LIMIT);

        assertThat(topics).extracting(GenerationTopic::category)
                .containsExactly(Category.DB, Category.NETWORK, Category.OS);
    }

    @Test
    @DisplayName("요청 개수가 카테고리 상한보다 작으면 요청 개수만큼만 고른다.")
    void select_respectsLimit() {
        Map<Category, Double> scores = Map.of(Category.DB, 0.9, Category.NETWORK, 0.8);

        assertThat(policy.select(profile(scores, List.of()), 1)).hasSize(1);
    }

    @Test
    @DisplayName("신뢰 표본을 넘긴 태그를 약점도 순으로 최대 2개까지 붙인다.")
    void select_attachesTrustedTags() {
        Map<Category, Double> scores = Map.of(Category.DB, 0.9);
        List<TagWeakness> tags = List.of(
                new TagWeakness("인덱스", Category.DB, 0.9, 3),
                new TagWeakness("트랜잭션", Category.DB, 0.8, 2),
                new TagWeakness("정규화", Category.DB, 0.7, 2)
        );

        List<GenerationTopic> topics = policy.select(profile(scores, tags), LIMIT);

        assertThat(topics).singleElement()
                .satisfies(topic -> assertThat(topic.tags()).containsExactly("인덱스", "트랜잭션"));
    }

    @Test
    @DisplayName("표본이 신뢰 기준에 못 미치는 태그는 붙이지 않는다.")
    void select_skipsUntrustedTags() {
        Map<Category, Double> scores = Map.of(Category.DB, 0.9);
        List<TagWeakness> tags = List.of(new TagWeakness("인덱스", Category.DB, 0.9, 1));

        List<GenerationTopic> topics = policy.select(profile(scores, tags), LIMIT);

        // 태그가 없어도 카테고리만으로 생성한다.
        assertThat(topics).singleElement().satisfies(topic -> {
            assertThat(topic.tags()).isEmpty();
            assertThat(topic.reason()).contains("신뢰할 태그 표본 없음");
        });
    }

    @Test
    @DisplayName("다른 카테고리의 태그는 붙이지 않는다.")
    void select_skipsOtherCategoryTags() {
        Map<Category, Double> scores = Map.of(Category.DB, 0.9);
        List<TagWeakness> tags = List.of(new TagWeakness("TCP", Category.NETWORK, 0.9, 3));

        assertThat(policy.select(profile(scores, tags), LIMIT))
                .singleElement()
                .satisfies(topic -> assertThat(topic.tags()).isEmpty());
    }

    @Test
    @DisplayName("약점이 클수록 낮은 목표 난이도를 준다.")
    void select_targetDifficulty() {
        assertThat(difficultyOf(0.7)).isEqualTo(Difficulty.LOW);
        assertThat(difficultyOf(0.69)).isEqualTo(Difficulty.MEDIUM);
        assertThat(difficultyOf(0.35)).isEqualTo(Difficulty.MEDIUM);
        assertThat(difficultyOf(0.34)).isEqualTo(Difficulty.HIGH);
    }

    @Test
    @DisplayName("약점도가 같으면 카테고리 순서를 고정해 같은 프로필이면 같은 결과가 나온다.")
    void select_tieBreak() {
        Map<Category, Double> scores = new LinkedHashMap<>();
        scores.put(Category.OS, 0.5);
        scores.put(Category.DB, 0.5);
        scores.put(Category.NETWORK, 0.5);

        List<GenerationTopic> first = policy.select(profile(scores, List.of()), LIMIT);
        List<GenerationTopic> second = policy.select(profile(scores, List.of()), LIMIT);

        // Category ordinal 순서(DB -> NETWORK -> OS)로 고정된다.
        assertThat(first).extracting(GenerationTopic::category)
                .containsExactly(Category.DB, Category.NETWORK, Category.OS);
        assertThat(second).isEqualTo(first);
    }

    @Test
    @DisplayName("약점도가 같은 태그는 이름 오름차순으로 붙인다.")
    void select_tagTieBreak() {
        Map<Category, Double> scores = Map.of(Category.DB, 0.9);
        List<TagWeakness> tags = List.of(
                new TagWeakness("정규화", Category.DB, 0.8, 2),
                new TagWeakness("index", Category.DB, 0.8, 2),
                new TagWeakness("트랜잭션", Category.DB, 0.8, 2)
        );

        assertThat(policy.select(profile(scores, tags), LIMIT))
                .singleElement()
                .satisfies(topic -> assertThat(topic.tags()).containsExactly("index", "정규화"));
    }

    @Test
    @DisplayName("풀이 이력이 없는 프로필이면 생성 주제를 만들지 않는다.")
    void select_emptyProfile() {
        assertThat(policy.select(WeaknessProfile.empty(), LIMIT)).isEmpty();
    }

    @Test
    @DisplayName("요청 개수가 0 이하면 생성 주제를 만들지 않는다.")
    void select_nonPositiveLimit() {
        assertThat(policy.select(profile(Map.of(Category.DB, 0.9), List.of()), 0)).isEmpty();
    }

    private Difficulty difficultyOf(double weaknessScore) {
        return policy.select(profile(Map.of(Category.DB, weaknessScore), List.of()), LIMIT)
                .get(0)
                .targetDifficulty();
    }

    private WeaknessProfile profile(Map<Category, Double> categoryScores, List<TagWeakness> tagWeaknesses) {
        return new WeaknessProfile(categoryScores, tagWeaknesses, 10);
    }
}
