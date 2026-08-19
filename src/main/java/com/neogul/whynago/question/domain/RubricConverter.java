package com.neogul.whynago.question.domain;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

// 루브릭은 항목·배점·꼬리질문 범위가 얽힌 중첩 구조이고 조건 검색 대상이 아니라, 테이블로 쪼개지 않고
// 한 컬럼에 JSON으로 담는다. tools/question-pipeline이 내보내는 UPDATE SQL의 형식이 그대로 이 형식이다.
//
// AttributeConverter는 스프링 빈이 아니라 주입을 받을 수 없어 ObjectMapper를 직접 들고 쓴다.
@Slf4j
@Converter
public class RubricConverter implements AttributeConverter<Rubric, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public String convertToDatabaseColumn(Rubric rubric) {
        if (rubric == null || rubric.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(rubric);
        } catch (Exception e) {
            log.warn("루브릭 직렬화 실패 - criteriaCount={}, cause={}", rubric.size(), e.toString());
            return null;
        }
    }

    // 루브릭 하나가 깨졌다고 문항 조회 전체가 죽으면 안 된다. null로 돌려 기준 없는 채점으로 폴백시킨다.
    @Override
    public Rubric convertToEntityAttribute(String column) {
        if (column == null || column.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(column, Rubric.class);
        } catch (Exception e) {
            log.warn("루브릭 역직렬화 실패 - cause={}", e.toString());
            return null;
        }
    }
}
