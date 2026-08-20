package com.neogul.whynago.question.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.fixture.RubricFixture;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RubricConverterTest {

    private final RubricConverter rubricConverter = new RubricConverter();

    @Test
    @DisplayName("직렬화한 루브릭을 되읽어 같은 값으로 복원한다.")
    void convert_roundTripLosesValue() {
        Rubric rubric = RubricFixture.threeCriteria();

        String column = rubricConverter.convertToDatabaseColumn(rubric);

        assertThat(rubricConverter.convertToEntityAttribute(column)).isEqualTo(rubric);
    }

    @Test
    @DisplayName("파이프라인이 내보낸 JSON을 그대로 읽어 들인다.")
    void convertToEntityAttribute_pipelineJson() {
        String column = """
                {
                  "criteria": [
                    { "point": "TCP는 신뢰성 있는 전송에 쓴다.", "weight": 6 },
                    { "point": "UDP는 저지연 통신에 쓴다.", "weight": 4 }
                  ],
                  "followupScope": {
                    "allowed": ["흐름 제어"],
                    "forbidden": ["TCP/IP 계층 구조"]
                  }
                }
                """;

        Rubric rubric = rubricConverter.convertToEntityAttribute(column);

        assertThat(rubric.criteria())
                .extracting(RubricCriterion::point, RubricCriterion::weight)
                .containsExactly(
                        tuple("TCP는 신뢰성 있는 전송에 쓴다.", 6),
                        tuple("UDP는 저지연 통신에 쓴다.", 4));
        assertThat(rubric.followupScope().allowed()).containsExactly("흐름 제어");
        assertThat(rubric.followupScope().forbidden()).containsExactly("TCP/IP 계층 구조");
    }

    @Test
    @DisplayName("루브릭이 없거나 항목이 비어 있으면 컬럼을 비운다.")
    void convertToDatabaseColumn_emptyRubric() {
        assertThat(rubricConverter.convertToDatabaseColumn(null)).isNull();
        assertThat(rubricConverter.convertToDatabaseColumn(new Rubric(List.of(), null))).isNull();
    }

    @Test
    @DisplayName("컬럼이 비어 있으면 루브릭 없이 읽는다.")
    void convertToEntityAttribute_blankColumn() {
        assertThat(rubricConverter.convertToEntityAttribute(null)).isNull();
        assertThat(rubricConverter.convertToEntityAttribute("  ")).isNull();
    }

    @Test
    @DisplayName("깨진 JSON은 예외를 던지지 않고 루브릭 없음으로 읽는다.")
    void convertToEntityAttribute_brokenJsonThrows() {
        assertThat(rubricConverter.convertToEntityAttribute("{\"criteria\": [")).isNull();
    }
}
