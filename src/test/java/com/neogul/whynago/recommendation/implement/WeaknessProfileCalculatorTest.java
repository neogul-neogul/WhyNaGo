package com.neogul.whynago.recommendation.implement;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.EssaySolvedFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.fixture.SolvedMultipleChoiceFixture;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.infra.TagRepository;
import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.mastery.service.MasteryService;
import com.neogul.whynago.mastery.service.dto.RecordMasteryCommand;
import com.neogul.whynago.recommendation.domain.MasteryWeight;
import com.neogul.whynago.recommendation.domain.TagWeakness;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WeaknessProfileCalculatorTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;
    private static final int MAIN_TURN = 1;

    @Autowired
    private WeaknessProfileCalculator weaknessProfileCalculator;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private MasteryService masteryService;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("풀이 이력이 없으면 빈 프로필을 만든다.")
    void calculate_withoutHistory() {
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        assertThat(profile.isEmpty()).isTrue();
        assertThat(profile.categoryScores()).isEmpty();
    }

    @Test
    @DisplayName("카테고리 약점도는 그 카테고리 문항들의 숙련도 가중치 평균이다.")
    void calculate_categoryScore() {
        // given
        Question mastered = saveQuestion();
        Question notLearned = saveQuestion();
        // 통계가 없으면 기본값 180초가 기준이다. 60초 정답은 MASTERED, 300초 오답은 NOT_LEARNED다.
        solveMultipleChoice(mastered, true, 60);
        solveMultipleChoice(notLearned, false, 300);

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        double expected = (MasteryWeight.of(MasteryLevel.MASTERED) + MasteryWeight.of(MasteryLevel.NOT_LEARNED)) / 2;
        assertThat(profile.categoryScores()).containsEntry(Category.DB, expected);
        assertThat(profile.solvedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("표본이 2건 이상인 태그는 태그 자체의 약점도를 갖는다.")
    void calculate_trustedTag() {
        // given
        Question first = saveQuestion();
        Question second = saveQuestion();
        tag(first, "인덱스");
        tag(second, "인덱스");
        solveMultipleChoice(first, false, 300);
        solveMultipleChoice(second, false, 300);

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.tagWeaknesses()).singleElement().satisfies(tag -> {
            assertThat(tag.trusted()).isTrue();
            assertThat(tag.sampleCount()).isEqualTo(2);
            assertThat(tag.weaknessScore()).isEqualTo(MasteryWeight.of(MasteryLevel.NOT_LEARNED));
        });
    }

    @Test
    @DisplayName("표본이 2건 미만인 태그는 소속 카테고리 약점도로 폴백한다.")
    void calculate_untrustedTagFallsBackToCategory() {
        // given
        Question tagged = saveQuestion();
        Question untagged = saveQuestion();
        tag(tagged, "인덱스");
        // 태그 표본은 1건(오답)이지만 카테고리 표본은 2건(오답 + 정답)이다.
        solveMultipleChoice(tagged, false, 300);
        solveMultipleChoice(untagged, true, 60);

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        double categoryScore = profile.categoryScores().get(Category.DB);
        assertThat(profile.tagWeaknesses()).singleElement().satisfies(tag -> {
            assertThat(tag.trusted()).isFalse();
            // 태그 자체 값(1.0)이 아니라 카테고리 평균으로 대체된다.
            assertThat(tag.weaknessScore()).isEqualTo(categoryScore);
            assertThat(tag.weaknessScore()).isNotEqualTo(MasteryWeight.of(MasteryLevel.NOT_LEARNED));
        });
    }

    @Test
    @DisplayName("서술형 꼬리질문은 참조할 문항이 없어 프로필에 반영되지 않는다.")
    void calculate_excludesFollowup() {
        // given
        Question question = saveQuestion();
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .userId(USER_ID).questionId(question.getId()).isCorrect(true).score(9).elapsedSeconds(60).build());
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .userId(USER_ID).type(ItemType.FOLLOWUP).questionId(null).isCorrect(false).score(1).build());

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.solvedCount()).isEqualTo(1);
        assertThat(profile.categoryScores()).containsEntry(Category.DB, MasteryWeight.of(MasteryLevel.MASTERED));
    }

    @Test
    @DisplayName("서술형 저점수는 시간과 무관하게 개념 없음으로 반영된다.")
    void calculate_reflectsEssayScore() {
        // given
        Question question = saveQuestion();
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .userId(USER_ID).questionId(question.getId()).isCorrect(true).score(2).elapsedSeconds(10).build());

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.categoryScores()).containsEntry(Category.DB, MasteryWeight.of(MasteryLevel.NOT_LEARNED));
    }

    @Test
    @DisplayName("다른 사용자의 풀이 이력은 프로필에 섞이지 않는다.")
    void calculate_ignoresOtherUsers() {
        // given
        Question question = saveQuestion();
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .userId(USER_ID + 1).questionId(question.getId()).isCorrect(false).elapsedSeconds(300).build());

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.isEmpty()).isTrue();
    }

    @Test
    @DisplayName("태그 신뢰 기준은 표본 2건이다.")
    void trustedSampleThreshold() {
        assertThat(TagWeakness.MIN_TRUSTED_SAMPLE).isEqualTo(2);
    }

    private Question saveQuestion() {
        return questionRepository.save(QuestionFixture.essayRoot());
    }

    // tag.name은 유일하므로 같은 태그를 여러 문항에 붙일 때는 사전 행을 재사용한다.
    private void tag(Question question, String name) {
        Tag tag = tagRepository.findByNameIn(List.of(name)).stream()
                .findFirst()
                .orElseGet(() -> tagRepository.save(TagFixture.db(name)));
        questionTagRepository.save(QuestionTag.create(question.getId(), tag.getId()));
    }

    private void solveMultipleChoice(Question question, boolean correct, int elapsedSeconds) {
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .userId(USER_ID)
                .questionId(question.getId())
                .isCorrect(correct)
                .elapsedSeconds(elapsedSeconds)
                .build());
    }

    @Test
    @DisplayName("서술형은 AI가 판정한 숙련도를 시간 기반 판정보다 우선해 반영한다.")
    void calculate_prefersAiMastery() {
        // given
        Question question = saveQuestion();
        // 시간 기반으로는 60초 정답이라 MASTERED(가중치 0)로 잡히는 이력이다.
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .userId(USER_ID).questionId(question.getId()).isCorrect(true).score(9).elapsedSeconds(60).build());
        // 그런데 채점 AI는 근거가 흔들린다고 판정했다.
        masteryService.record(RecordMasteryCommand.ofEssay(
                USER_ID,
                question.getId(),
                Category.DB,
                List.of(),
                MasteryLevel.UNSTABLE,
                "결론은 맞지만 이유가 틀렸다",
                MAIN_TURN
        ));

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.categoryScores())
                .containsEntry(Category.DB, MasteryWeight.of(MasteryLevel.UNSTABLE));
    }

    @Test
    @DisplayName("AI 판정이 없는 서술형 이력은 시간 기반 판정으로 폴백한다.")
    void calculate_fallsBackWithoutAiMastery() {
        // given
        Question question = saveQuestion();
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .userId(USER_ID).questionId(question.getId()).isCorrect(true).score(9).elapsedSeconds(60).build());

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.categoryScores())
                .containsEntry(Category.DB, MasteryWeight.of(MasteryLevel.MASTERED));
    }

    @Test
    @DisplayName("다른 사용자의 AI 판정은 내 프로필에 반영되지 않는다.")
    void calculate_ignoresOtherUsersMastery() {
        // given
        Question question = saveQuestion();
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .userId(USER_ID).questionId(question.getId()).isCorrect(true).score(9).elapsedSeconds(60).build());
        masteryService.record(RecordMasteryCommand.ofEssay(
                USER_ID + 1, question.getId(), Category.DB, List.of(),
                MasteryLevel.NOT_LEARNED, "개념이 없다", MAIN_TURN));

        // when
        WeaknessProfile profile = weaknessProfileCalculator.calculate(USER_ID);

        // then
        assertThat(profile.categoryScores())
                .containsEntry(Category.DB, MasteryWeight.of(MasteryLevel.MASTERED));
    }
}
