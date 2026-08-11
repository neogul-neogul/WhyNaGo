package com.neogul.whynago.progress.implement;

import com.neogul.whynago.progress.implement.dto.CategoryProgress;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.implement.QuestionReader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 유저의 카테고리별 집계에 문제은행의 카테고리별 전체 문항 수를 붙인다.
 * 기록이 없는 카테고리도 0으로 채워 항상 모든 카테고리를 반환한다.
 */
@Component
@RequiredArgsConstructor
public class CategoryProgressAssembler {

    private final QuestionReader questionReader;

    public List<CategoryProgress> assemble(UserProgressAggregate aggregate) {
        Map<Category, Integer> totalCounts = questionReader.countByCategory();

        return Arrays.stream(Category.values())
                .map(category -> new CategoryProgress(
                        category,
                        totalCounts.getOrDefault(category, 0),
                        aggregate.categoryQuestionCounts().getOrDefault(category, 0),
                        aggregate.categoryCorrectCounts().getOrDefault(category, 0),
                        aggregate.categoryScores().getOrDefault(category, 0)
                ))
                .toList();
    }
}
