package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EssaySolvedReader {

    private final EssaySolvedRepository essaySolvedRepository;

    public List<EssaySolved> readOrdered(Long solvedSessionId) {
        return essaySolvedRepository.findBySolvedSessionIdOrderBySequence(solvedSessionId);
    }

    public List<Long> readSolvedQuestionIds(Long userId) {
        return essaySolvedRepository.findSolvedQuestionIds(userId);
    }

    // 꼬리질문은 questionId가 없어 태그·카테고리를 붙일 수 없으므로 본질문만 읽는다.
    public List<EssaySolved> readMainByUser(Long userId) {
        return essaySolvedRepository.findByUserIdAndType(userId, ItemType.MAIN);
    }
}
