package com.neogul.whynago.question.implement;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.domain.ChoiceMasteryReason;
import com.neogul.whynago.mastery.domain.MasteryPolicy;
import com.neogul.whynago.mastery.domain.SolvedSignal;
import com.neogul.whynago.mastery.service.MasteryService;
import com.neogul.whynago.mastery.service.dto.RecordMasteryCommand;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.implement.dto.ChoiceSolveSignal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 객관식 풀이의 숙련도를 그 문항의 태그·카테고리에 연결해 기록한다. 서술형의 EssayMasteryRecorder와
// 같은 자리에 두는 이유는 태그·카테고리 해석이 question 도메인의 일이기 때문이다.
//
// 서술형과 달리 AI를 부르지 않고 MasteryPolicy가 정답 여부 x 평균 대비 소요시간으로 판정한다.
// 판정에 쓰는 정답 여부는 클라이언트가 보고한 값이 아니라 서버가 보기를 다시 채점한 결과다.
@Component
@RequiredArgsConstructor
public class ChoiceMasteryRecorder {

    private final QuestionReader questionReader;
    private final QuestionStatReader questionStatReader;
    private final QuestionTagIdReader questionTagIdReader;
    private final MasteryPolicy masteryPolicy;
    private final MasteryService masteryService;

    // 한 세션의 풀이를 한 번에 받는다. 문항·통계를 건별로 조회하면 세션 크기만큼 쿼리가 늘어난다.
    public void recordAll(Long userId, List<ChoiceSolveSignal> signals) {
        if (signals.isEmpty()) {
            return;
        }

        List<Long> questionIds = signals.stream().map(ChoiceSolveSignal::questionId).distinct().toList();
        Map<Long, Question> questions = questionReader.readAll(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        Map<Long, QuestionStat> stats = questionStatReader.readAll(questionIds);

        for (ChoiceSolveSignal signal : signals) {
            Question question = questions.get(signal.questionId());
            if (question == null) {
                continue;
            }
            masteryService.record(command(userId, question, level(signal, stats.get(signal.questionId()))));
        }
    }

    private MasteryLevel level(ChoiceSolveSignal signal, QuestionStat stat) {
        // 통계가 없거나 표본이 모자라면 MasteryPolicy가 기본 기준 시간으로 판정한다.
        Integer avgElapsedSeconds = stat == null ? null : stat.getAvgElapsedSeconds();
        int sampleCount = stat == null ? 0 : stat.getSampleCount();
        return masteryPolicy.judge(
                SolvedSignal.of(signal.correct(), null, signal.elapsedSeconds()),
                avgElapsedSeconds,
                sampleCount
        );
    }

    private RecordMasteryCommand command(Long userId, Question question, MasteryLevel level) {
        return RecordMasteryCommand.ofChoice(
                userId,
                question.getId(),
                question.getCategory(),
                questionTagIdReader.readTagIds(question.getId()),
                level,
                ChoiceMasteryReason.of(level)
        );
    }
}
