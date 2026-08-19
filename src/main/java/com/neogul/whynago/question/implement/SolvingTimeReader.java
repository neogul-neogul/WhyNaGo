package com.neogul.whynago.question.implement;

import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.domain.SolvingTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 클라이언트가 보고한 소요시간을 그 문항의 평균과 묶어 채점에 쓸 신호로 만든다.
// 평균과의 비교가 판정의 전부이므로 통계 조회를 여기서 함께 처리한다.
@Component
@RequiredArgsConstructor
public class SolvingTimeReader {

    private final QuestionStatReader questionStatReader;

    public SolvingTime read(Long questionId, Integer rawElapsedSeconds) {
        if (rawElapsedSeconds == null) {
            // 시간을 안 보냈으면 평균을 읽을 이유가 없다. 채점 경로의 쿼리 한 번을 아낀다.
            return SolvingTime.unmeasured();
        }
        return questionStatReader.read(questionId)
                .map(stat -> SolvingTime.of(
                        rawElapsedSeconds, stat.getAvgElapsedSeconds(), stat.getSampleCount()))
                .orElseGet(() -> SolvingTime.of(rawElapsedSeconds, null, 0));
    }
}
