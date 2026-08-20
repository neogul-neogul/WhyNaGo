package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.Rubric;
import com.neogul.whynago.question.domain.RubricCriterion;
import com.neogul.whynago.question.implement.dto.RubricEvaluation;
import com.neogul.whynago.question.implement.dto.RubricGrading;
import com.neogul.whynago.question.infra.ai.CriterionGrading;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

// AI가 돌려준 항목별 판정을 루브릭과 이어 붙이고, 충족 항목의 배점 합으로 점수를 계산한다.
//
// 파싱·정합 규칙을 EssayAnswerEvaluator에 두면 평가기가 채점 흐름과 응답 해석을 같이 들게 되어 비대해진다.
@Slf4j
@Component
public class RubricGradingResolver {

    // 루브릭이 없거나 AI 응답이 루브릭과 맞지 않으면 판정을 버리고 AI 점수를 그대로 쓴다.
    // 채점 자체를 실패시키지 않는다 - 숙련도를 못 받았을 때 채점은 그대로 응답하는 기존 정책과 같은 태도다.
    public RubricGrading resolve(Rubric rubric, List<CriterionGrading> gradings, int aiScore) {
        if (rubric == null || rubric.isEmpty()) {
            return RubricGrading.withoutRubric(aiScore);
        }
        if (!matches(rubric, gradings)) {
            log.warn("루브릭 채점 결과가 기준과 맞지 않아 AI 점수로 폴백한다 - criteriaCount={}, resultCount={}",
                    rubric.size(), gradings == null ? 0 : gradings.size());
            return RubricGrading.withoutRubric(aiScore);
        }

        Set<Integer> metIndexes = gradings.stream()
                .filter(CriterionGrading::met)
                .map(CriterionGrading::index)
                .collect(Collectors.toUnmodifiableSet());

        return new RubricGrading(rubric.scoreOf(metIndexes), join(rubric, gradings));
    }

    private boolean matches(Rubric rubric, List<CriterionGrading> gradings) {
        if (gradings == null || gradings.size() != rubric.size()) {
            return false;
        }
        Set<Integer> seen = new HashSet<>();
        for (CriterionGrading grading : gradings) {
            if (grading.index() < 1 || grading.index() > rubric.size() || !seen.add(grading.index())) {
                return false;
            }
        }
        return true;
    }

    // 응답 순서가 뒤섞여 와도 사용자에게는 루브릭에 적힌 순서로 보여야 하므로, 번호로 찾아 다시 세운다.
    private List<RubricEvaluation> join(Rubric rubric, List<CriterionGrading> gradings) {
        Map<Integer, CriterionGrading> byIndex = gradings.stream()
                .collect(Collectors.toMap(CriterionGrading::index, Function.identity()));
        List<RubricCriterion> criteria = rubric.criteria();

        return IntStream.rangeClosed(1, criteria.size())
                .mapToObj(index -> {
                    RubricCriterion criterion = criteria.get(index - 1);
                    CriterionGrading grading = byIndex.get(index);
                    return new RubricEvaluation(
                            criterion.point(), criterion.weight(), grading.met(), grading.reason());
                })
                .toList();
    }
}
