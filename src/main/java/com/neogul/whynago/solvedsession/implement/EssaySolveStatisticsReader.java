package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.dto.QuestionSolveCount;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssaySolveStatisticsReader {

    private final EssaySolvedRepository essaySolvedRepository;

    // 관리자 문제 목록에서 여러 서술형 문제의 풀이수·정답률 컬럼을 한 번에 채울 때 쓴다.
    public Map<Long, QuestionSolveCount> readBulk(List<Long> questionIds) {
        if (questionIds.isEmpty()) {
            return Map.of();
        }
        return essaySolvedRepository.countGroupByQuestion(questionIds).stream()
                .collect(Collectors.toMap(QuestionSolveCount::getQuestionId, Function.identity()));
    }

    // 관리자 문제 상세에서 서술형 한 문제의 풀이수·정답률을 조회할 때 쓴다. 풀이 기록이 없으면 null이다.
    public QuestionSolveCount read(Long questionId) {
        return readBulk(List.of(questionId)).get(questionId);
    }
}
