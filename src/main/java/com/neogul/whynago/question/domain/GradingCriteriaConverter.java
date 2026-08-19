package com.neogul.whynago.question.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Arrays;
import java.util.List;

// 채점 기준은 순서 있는 짧은 문장 목록이고 조건 검색 대상이 아니라, 별도 테이블 대신 한 컬럼에 줄바꿈으로 잇는다.
@Converter
public class GradingCriteriaConverter implements AttributeConverter<List<String>, String> {

    private static final String DELIMITER = "\n";

    @Override
    public String convertToDatabaseColumn(List<String> criteria) {
        if (criteria == null || criteria.isEmpty()) {
            return null;
        }
        return String.join(DELIMITER, criteria);
    }

    @Override
    public List<String> convertToEntityAttribute(String column) {
        if (column == null || column.isBlank()) {
            return List.of();
        }
        return Arrays.stream(column.split(DELIMITER))
                .map(String::trim)
                .filter(criterion -> !criterion.isEmpty())
                .toList();
    }
}
