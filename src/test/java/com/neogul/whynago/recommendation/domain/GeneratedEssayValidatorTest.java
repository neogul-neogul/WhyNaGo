package com.neogul.whynago.recommendation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GeneratedEssayValidatorTest {

    private static final String TITLE = "인덱스 카디널리티";
    private static final String CONTENT = "카디널리티가 낮은 컬럼에 인덱스를 걸면 어떤 일이 벌어지는지 설명하라.";
    private static final String MODEL_ANSWER = "카디널리티가 낮으면 인덱스를 타도 걸러지는 행이 적어 풀스캔보다 느려질 수 있다.";
    private static final Set<String> KNOWN_TAGS = Set.of("인덱스", "트랜잭션", "정규화");

    private final GeneratedEssayValidator validator = new GeneratedEssayValidator();

    @Test
    @DisplayName("형식과 요청 조건을 모두 지킨 문항은 위반 사유가 없다.")
    void validate() {
        assertThat(validate(candidate())).isEmpty();
    }

    @Test
    @DisplayName("발문이 최소 길이에 못 미치면 버린다.")
    void validate_shortContent() {
        GeneratedEssay candidate = withContent("짧다.");

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("발문"));
    }

    @Test
    @DisplayName("발문이 비어 있으면 버린다.")
    void validate_blankContent() {
        assertThat(validate(withContent("   "))).anyMatch(violation -> violation.contains("발문"));
    }

    @Test
    @DisplayName("모범답안이 최소 길이에 못 미치면 버린다.")
    void validate_shortModelAnswer() {
        GeneratedEssay candidate = new GeneratedEssay(
                TITLE, CONTENT, "짧은 모범답안", criteria(), Category.DB, Difficulty.MEDIUM, List.of("인덱스"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("모범답안"));
    }

    @Test
    @DisplayName("채점 기준이 2개 미만이면 버린다.")
    void validate_notEnoughGradingCriteria() {
        GeneratedEssay candidate = new GeneratedEssay(
                TITLE, CONTENT, MODEL_ANSWER, List.of("하나뿐"), Category.DB, Difficulty.MEDIUM, List.of("인덱스"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("채점 기준"));
    }

    @Test
    @DisplayName("요청한 카테고리와 다르면 버린다.")
    void validate_categoryMismatch() {
        GeneratedEssay candidate = new GeneratedEssay(
                TITLE, CONTENT, MODEL_ANSWER, criteria(), Category.NETWORK, Difficulty.MEDIUM, List.of("인덱스"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("카테고리"));
    }

    @Test
    @DisplayName("요청한 목표 난이도와 다르면 버린다.")
    void validate_difficultyMismatch() {
        GeneratedEssay candidate = new GeneratedEssay(
                TITLE, CONTENT, MODEL_ANSWER, criteria(), Category.DB, Difficulty.HIGH, List.of("인덱스"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("난이도"));
    }

    @Test
    @DisplayName("태그가 없으면 버린다.")
    void validate_withoutTags() {
        assertThat(validate(withTags(List.of()))).anyMatch(violation -> violation.contains("태그 수"));
    }

    @Test
    @DisplayName("태그가 3개 이상이면 버린다.")
    void validate_tooManyTags() {
        GeneratedEssay candidate = withTags(List.of("인덱스", "트랜잭션", "정규화"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("태그 수"));
    }

    @Test
    @DisplayName("태그 사전에 없는 태그를 만들면 버린다.")
    void validate_unknownTag() {
        GeneratedEssay candidate = withTags(List.of("인덱스", "옵티마이저튜닝"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("사전에 없는 태그"));
    }

    @Test
    @DisplayName("요청한 태그를 하나도 포함하지 않으면 버린다.")
    void validate_missingRequestedTag() {
        // 사전에는 있지만 이번 요청의 취약 태그가 아니다. 취약 주제를 벗어난 문항이므로 버린다.
        GeneratedEssay candidate = withTags(List.of("정규화"));

        assertThat(validate(candidate)).anyMatch(violation -> violation.contains("요청한 태그"));
    }

    @Test
    @DisplayName("요청한 태그가 없던 주제라면 사전에 있는 태그만으로도 통과한다.")
    void validate_withoutRequestedTags() {
        GenerationTopic topic = new GenerationTopic(Category.DB, List.of(), Difficulty.MEDIUM, 0.5, "이유");

        List<String> violations = validator.validate(
                withTags(List.of("정규화")), topic, KNOWN_TAGS, Set.of());

        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("기존 문항 제목과 완전히 같으면 버린다.")
    void validate_duplicatedTitle() {
        List<String> violations = validator.validate(candidate(), topic(), KNOWN_TAGS, Set.of(TITLE));

        assertThat(violations).anyMatch(violation -> violation.contains("제목"));
    }

    @Test
    @DisplayName("대소문자·공백만 다른 제목도 중복으로 본다.")
    void validate_duplicatedTitleIgnoringCase() {
        List<String> violations = validator.validate(candidate(), topic(), KNOWN_TAGS, Set.of("  " + TITLE + " "));

        assertThat(violations).anyMatch(violation -> violation.contains("제목"));
    }

    @Test
    @DisplayName("기존 문항 발문과 완전히 같으면 버린다.")
    void validate_duplicatedContent() {
        List<String> violations = validator.validate(candidate(), topic(), KNOWN_TAGS, Set.of(CONTENT));

        assertThat(violations).anyMatch(violation -> violation.contains("발문"));
    }

    private List<String> validate(GeneratedEssay candidate) {
        return validator.validate(candidate, topic(), KNOWN_TAGS, Set.of());
    }

    private GenerationTopic topic() {
        return new GenerationTopic(Category.DB, List.of("인덱스"), Difficulty.MEDIUM, 0.5, "이유");
    }

    private GeneratedEssay candidate() {
        return withTags(List.of("인덱스"));
    }

    private GeneratedEssay withTags(List<String> tags) {
        return new GeneratedEssay(TITLE, CONTENT, MODEL_ANSWER, criteria(), Category.DB, Difficulty.MEDIUM, tags);
    }

    private GeneratedEssay withContent(String content) {
        return new GeneratedEssay(
                TITLE, content, MODEL_ANSWER, criteria(), Category.DB, Difficulty.MEDIUM, List.of("인덱스"));
    }

    private List<String> criteria() {
        return List.of("카디널리티 정의", "옵티마이저가 인덱스를 버리는 이유");
    }
}
