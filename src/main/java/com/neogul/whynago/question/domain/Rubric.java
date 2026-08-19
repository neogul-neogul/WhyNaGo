package com.neogul.whynago.question.domain;

import java.util.List;
import java.util.Set;

// 서술형 채점 기준. tools/question-pipeline이 생성해 question.rubric에 JSON으로 넣는다.
//
// 파이프라인이 criteria 3~5개·weight 합 정확히 10을 보장하므로(validate_rubric), 충족한 항목의
// 배점 합이 그대로 0~10 점수가 된다. 그래서 점수를 AI 재량에 맡기지 않고 서버가 계산할 수 있다.
public record Rubric(List<RubricCriterion> criteria, FollowupScope followupScope) {

    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 10;

    public List<RubricCriterion> criteria() {
        return criteria == null ? List.of() : criteria;
    }

    public boolean isEmpty() {
        return criteria().isEmpty();
    }

    public int size() {
        return criteria().size();
    }

    // metIndexes는 1부터 시작하는 항목 번호다. AI가 그 번호로 판정을 돌려주므로 그대로 받는다.
    //
    // 배점 합이 10이 아닌 루브릭이 어쩌다 들어와도 점수가 0~10을 벗어나면 안 된다.
    // API 규격과 통과 임계값(7)이 이 범위를 전제로 하기 때문에 여기서 잘라 낸다.
    public int scoreOf(Set<Integer> metIndexes) {
        int score = 0;
        List<RubricCriterion> criteria = criteria();
        for (int index = 1; index <= criteria.size(); index++) {
            if (metIndexes.contains(index)) {
                score += criteria.get(index - 1).weight();
            }
        }
        return Math.clamp(score, MIN_SCORE, MAX_SCORE);
    }

    public boolean hasFollowupScope() {
        return followupScope != null && !followupScope.isEmpty();
    }
}
