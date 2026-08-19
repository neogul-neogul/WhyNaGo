package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import com.neogul.whynago.recommendation.implement.dto.CachedRecommendation;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 같은 사용자가 하루에 여러 번 조회해도 생성은 한 번이어야 한다.
// 캐시 키는 userId + 프로필 해시이며, 문제를 더 풀어 프로필이 바뀌면 즉시 무효가 된다.
// 유효 기간은 KST 자정까지다. Clock 빈이 KST이므로 LocalDate.now(clock)이 곧 서비스 기준 날짜다.
//
// 프로세스 메모리에 둔다. 인스턴스를 여럿 띄우면 인스턴스당 한 번씩 생성될 수 있으며,
// 재시작하면 캐시가 사라진다. 생성 문항 자체는 DB에 남으므로 중복 생성 비용만 문제가 된다.
@Component
@RequiredArgsConstructor
public class RecommendationCache {

    private final Map<Long, CachedRecommendation> cacheByUserId = new ConcurrentHashMap<>();

    private final QuestionReader questionReader;
    private final Clock clock;

    public List<Question> find(Long userId, WeaknessProfile profile) {
        CachedRecommendation cached = cacheByUserId.get(userId);
        if (cached == null || !cached.isValid(profile.hashCode(), LocalDate.now(clock))) {
            return List.of();
        }

        Map<Long, Question> questions = questionReader.readAll(cached.questionIds()).stream()
                // 검수에서 거절된 문항은 캐시에 남아 있어도 다시 내보내지 않는다.
                .filter(question -> !question.isRejected())
                .collect(Collectors.toMap(Question::getId, Function.identity()));

        // 한 건이라도 내보낼 수 없게 됐으면 캐시를 버리고 새로 만든다. 자리를 비운 채 돌려주지 않는다.
        if (questions.size() != cached.questionIds().size()) {
            cacheByUserId.remove(userId);
            return List.of();
        }
        // 조회 순서(id 오름차순)가 아니라 추천했던 순서를 유지한다. 생성 문항이 앞, 폴백이 뒤였다.
        return cached.questionIds().stream()
                .map(questions::get)
                .toList();
    }

    public void put(Long userId, WeaknessProfile profile, List<Question> questions) {
        if (questions.isEmpty()) {
            return;
        }
        cacheByUserId.put(userId, new CachedRecommendation(
                profile.hashCode(),
                questions.stream().map(Question::getId).toList(),
                LocalDate.now(clock)
        ));
    }
}
