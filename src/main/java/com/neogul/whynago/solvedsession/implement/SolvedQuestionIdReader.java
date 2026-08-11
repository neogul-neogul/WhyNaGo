package com.neogul.whynago.solvedsession.implement;

import java.util.List;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedQuestionIdReader {

    private final SolvedMultipleChoiceReader solvedMultipleChoiceReader;
    private final EssaySolvedReader essaySolvedReader;

    // 한 문제를 객관식·서술형 양쪽으로 풀 수는 없지만, 같은 문제를 여러 번 풀면 중복이 생기므로 합집합으로 모은다.
    public List<Long> readAll(Long userId) {
        return Stream.concat(
                        solvedMultipleChoiceReader.readSolvedQuestionIds(userId).stream(),
                        essaySolvedReader.readSolvedQuestionIds(userId).stream()
                )
                .distinct()
                .toList();
    }
}