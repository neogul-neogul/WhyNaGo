package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionStat;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.implement.QuestionStatReader;
import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.implement.MasteryReader;
import com.neogul.whynago.mastery.domain.MasteryPolicy;
import com.neogul.whynago.recommendation.domain.MasteryWeight;
import com.neogul.whynago.mastery.domain.SolvedSignal;
import com.neogul.whynago.recommendation.domain.TagWeakness;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import com.neogul.whynago.recommendation.implement.dto.SolvedQuestionSignal;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.implement.EssaySolvedReader;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// 사용자의 풀이 이력을 문항별 숙련도로 바꾸고, 카테고리·태그 2계층으로 약점도를 집계한다.
//
// 숙련도 판정은 2트랙이다.
//  - 서술형: 채점 AI가 답변 내용을 근거로 판정한 값을 그대로 쓴다.
//  - 객관식: AI를 호출하지 않으므로 정답 여부 x 소요시간 비율로 MasteryPolicy가 판정한다.
// 서술형인데 AI 판정이 없는 이력(프롬프트 v4 이전에 푼 것)은 MasteryPolicy로 폴백한다.
@Component
@RequiredArgsConstructor
public class WeaknessProfileCalculator {

    private final SolvedMultipleChoiceReader solvedMultipleChoiceReader;
    private final EssaySolvedReader essaySolvedReader;
    private final QuestionReader questionReader;
    private final QuestionStatReader questionStatReader;
    private final MasteryReader masteryReader;
    private final MasteryPolicy masteryPolicy;

    public WeaknessProfile calculate(Long userId) {
        List<SolvedQuestionSignal> signals = readSignals(userId);
        if (signals.isEmpty()) {
            return WeaknessProfile.empty();
        }

        List<Long> questionIds = signals.stream().map(SolvedQuestionSignal::questionId).distinct().toList();
        Map<Long, Question> questions = questionReader.readAll(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, Function.identity()));
        Map<Long, QuestionStat> stats = questionStatReader.readAll(questionIds);
        Map<Long, List<String>> tagNames = questionReader.readTagNames(questionIds);
        Map<Long, MasteryLevel> aiLevels = masteryReader.readLatestAiLevelsByQuestion(userId, questionIds);

        Map<Category, List<Double>> weightsByCategory = new LinkedHashMap<>();
        Map<String, List<Double>> weightsByTag = new LinkedHashMap<>();
        Map<String, Category> categoryByTag = new LinkedHashMap<>();

        for (SolvedQuestionSignal signal : signals) {
            Question question = questions.get(signal.questionId());
            if (question == null) {
                continue;
            }
            double weight = weightOf(signal, stats.get(signal.questionId()), aiLevels.get(signal.questionId()));

            weightsByCategory.computeIfAbsent(question.getCategory(), category -> new ArrayList<>()).add(weight);
            for (String tag : tagNames.getOrDefault(question.getId(), List.of())) {
                weightsByTag.computeIfAbsent(tag, name -> new ArrayList<>()).add(weight);
                categoryByTag.putIfAbsent(tag, question.getCategory());
            }
        }

        Map<Category, Double> categoryScores = average(weightsByCategory);
        return new WeaknessProfile(
                categoryScores,
                toTagWeaknesses(weightsByTag, categoryByTag, categoryScores),
                signals.size()
        );
    }

    private double weightOf(SolvedQuestionSignal signal, QuestionStat stat, MasteryLevel aiLevel) {
        // AI가 답변 내용을 보고 판정한 값이 있으면 그것이 우선이다. 시간 기반 판정보다 신호가 강하다.
        if (aiLevel != null) {
            return MasteryWeight.of(aiLevel);
        }
        Integer avgElapsedSeconds = stat == null ? null : stat.getAvgElapsedSeconds();
        int sampleCount = stat == null ? 0 : stat.getSampleCount();
        MasteryLevel level = masteryPolicy.judge(signal.signal(), avgElapsedSeconds, sampleCount);
        return MasteryWeight.of(level);
    }

    // 진단은 객관식·서술형 양쪽을 본다. 생성만 서술형 전용이다.
    private List<SolvedQuestionSignal> readSignals(Long userId) {
        return Stream.concat(
                        solvedMultipleChoiceReader.readAllByUser(userId).stream()
                                .map(WeaknessProfileCalculator::toSignal),
                        essaySolvedReader.readMainByUser(userId).stream()
                                .filter(solved -> solved.getQuestionId() != null)
                                .map(WeaknessProfileCalculator::toSignal)
                )
                .toList();
    }

    private static SolvedQuestionSignal toSignal(SolvedMultipleChoice solved) {
        return new SolvedQuestionSignal(
                solved.getQuestionId(),
                SolvedSignal.of(solved.isCorrect(), null, solved.getElapsedSeconds())
        );
    }

    private static SolvedQuestionSignal toSignal(EssaySolved solved) {
        return new SolvedQuestionSignal(
                solved.getQuestionId(),
                SolvedSignal.of(solved.isCorrect(), solved.getScore(), solved.getElapsedSeconds())
        );
    }

    // 표본이 신뢰 기준에 못 미치는 태그는 소속 카테고리 약점도로 폴백한다.
    private List<TagWeakness> toTagWeaknesses(
            Map<String, List<Double>> weightsByTag,
            Map<String, Category> categoryByTag,
            Map<Category, Double> categoryScores
    ) {
        return weightsByTag.entrySet().stream()
                .map(entry -> {
                    String tag = entry.getKey();
                    Category category = categoryByTag.get(tag);
                    int sampleCount = entry.getValue().size();
                    double score = sampleCount >= TagWeakness.MIN_TRUSTED_SAMPLE
                            ? mean(entry.getValue())
                            : categoryScores.getOrDefault(category, 0.0);
                    return new TagWeakness(tag, category, score, sampleCount);
                })
                .toList();
    }

    private Map<Category, Double> average(Map<Category, List<Double>> weights) {
        Map<Category, Double> averages = new LinkedHashMap<>();
        weights.forEach((category, values) -> averages.put(category, mean(values)));
        return averages;
    }

    private double mean(List<Double> values) {
        return values.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}
