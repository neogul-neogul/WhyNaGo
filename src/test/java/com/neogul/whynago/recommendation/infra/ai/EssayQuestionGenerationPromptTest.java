package com.neogul.whynago.recommendation.infra.ai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

// 프롬프트 템플릿은 코드가 넘기는 변수 목록과 어긋나면 런타임에야 깨진다.
// 중괄호를 실수로 넣는 것(Spring AI가 변수로 해석)도 여기서 먼저 잡는다.
class EssayQuestionGenerationPromptTest {

    private static final Map<String, Object> VARIABLES = Map.of(
            "category", "DB",
            "weakTags", "인덱스, 트랜잭션",
            "allowedTags", "인덱스, 트랜잭션, 정규화",
            "targetDifficulty", "LOW",
            "weaknessScore", "0.82",
            "reason", "DB 카테고리 약점도 0.82, 취약 태그: 인덱스, 트랜잭션",
            "wrongExplanations", "- 인덱스는 항상 조회를 빠르게 한다",
            "existingTitles", "- 인덱스와 실행 계획"
    );

    private final Resource template = new ClassPathResource("prompts/essay-question-generation.st");

    @Test
    @DisplayName("코드가 넘기는 변수로 프롬프트가 빈틈없이 렌더된다.")
    void render() {
        String rendered = new PromptTemplate(template).render(VARIABLES);

        // 미치환 placeholder나 실수로 넣은 중괄호가 남으면 안 된다.
        assertThat(rendered).doesNotContain("{").doesNotContain("}");
        assertThat(rendered).contains("DB").contains("LOW").contains("0.82");
    }

    @Test
    @DisplayName("변수가 하나라도 빠지면 렌더가 실패해 즉시 드러난다.")
    void render_missingVariable() {
        Map<String, Object> missing = VARIABLES.entrySet().stream()
                .filter(entry -> !entry.getKey().equals("weaknessScore"))
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        assertThatThrownBy(() -> new PromptTemplate(template).render(missing))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("오답 해설을 정답 근거로 쓰지 못하게 하는 라벨링 규칙이 들어 있다.")
    void render_containsMisconceptionLabeling() {
        String rendered = new PromptTemplate(template).render(VARIABLES);

        assertThat(rendered).contains("객관식 오답에 대한 해설");
        assertThat(rendered).contains("절대 정답으로 인용하지 않는다");
    }

    @Test
    @DisplayName("검증기가 버리는 기준과 목표 난이도의 의도를 프롬프트가 함께 설명한다.")
    void render_containsValidatorContractAndIntent() {
        String rendered = new PromptTemplate(template).render(VARIABLES);

        assertThat(rendered).contains("검증기 계약");
        assertThat(rendered).contains("재시도 없이 폐기된다");
        // 난이도를 낮추는 이유를 알려주지 않으면 모델이 "쉬운 함정 문제"를 만든다.
        assertThat(rendered).contains("변별이 아니라 개념 회복");
    }
}
