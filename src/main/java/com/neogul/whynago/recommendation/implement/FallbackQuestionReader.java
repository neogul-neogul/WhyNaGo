package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 생성이 부족하거나 실패했을 때 취약 주제에 해당하는 기존 문제은행 문항으로 자리를 채운다.
// 이때는 객관식·서술형을 모두 후보로 삼는다.
@Component
@RequiredArgsConstructor
public class FallbackQuestionReader {

    private static final int MAX_FALLBACK_CATEGORIES = 3;

    private final QuestionReader questionReader;

    public List<Question> fill(List<Question> generated, WeaknessProfile profile, int targetCount) {
        int shortage = targetCount - generated.size();
        if (shortage <= 0) {
            return generated;
        }

        List<Category> categories = profile.weakestCategories(MAX_FALLBACK_CATEGORIES);
        Set<Long> alreadyPicked = generated.stream().map(Question::getId).collect(Collectors.toSet());

        // 이미 고른 문항이 후보에 섞일 수 있으므로 부족분보다 넉넉히 읽고 걸러낸다.
        List<Question> fallback = questionReader
                .readApprovedByCategories(categories, shortage + alreadyPicked.size())
                .stream()
                .filter(question -> !alreadyPicked.contains(question.getId()))
                .limit(shortage)
                .toList();

        return Stream.concat(generated.stream(), fallback.stream()).toList();
    }
}
