package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionReader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 풀이 이력이 거의 없는 사용자에게 주는 문항이다. 프로필이 무의미하므로 AI를 호출하지 않고,
// 난이도 하 문항을 카테고리별로 고르게 나눠 준다.
@Component
@RequiredArgsConstructor
public class ColdStartQuestionReader {

    private final QuestionReader questionReader;

    public List<Question> read(int count) {
        Map<Category, Deque<Question>> byCategory = groupByCategory(
                questionReader.readApprovedByDifficulty(Difficulty.LOW));

        List<Question> picked = new ArrayList<>();
        // 카테고리를 한 바퀴씩 돌며 하나씩 뽑는다. 한 카테고리에서 몰아 뽑으면 "고르게"가 깨진다.
        while (picked.size() < count && byCategory.values().stream().anyMatch(queue -> !queue.isEmpty())) {
            for (Deque<Question> queue : byCategory.values()) {
                if (picked.size() >= count) {
                    break;
                }
                if (!queue.isEmpty()) {
                    picked.add(queue.poll());
                }
            }
        }
        return picked;
    }

    private Map<Category, Deque<Question>> groupByCategory(List<Question> questions) {
        Map<Category, Deque<Question>> byCategory = new LinkedHashMap<>();
        for (Question question : questions) {
            byCategory.computeIfAbsent(question.getCategory(), category -> new ArrayDeque<>()).add(question);
        }
        return byCategory;
    }
}
