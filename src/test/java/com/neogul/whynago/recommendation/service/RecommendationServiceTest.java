package com.neogul.whynago.recommendation.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.fixture.SolvedMultipleChoiceFixture;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionReviewStatus;
import com.neogul.whynago.question.domain.QuestionSource;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.infra.TagRepository;
import com.neogul.whynago.recommendation.service.dto.RecommendationResult;
import com.neogul.whynago.recommendation.service.dto.RecommendedQuestionResult;
import com.neogul.whynago.recommendation.service.dto.WeakTagResult;
import com.neogul.whynago.recommendation.service.dto.WeakTagsResult;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class RecommendationServiceTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Test
    @DisplayName("전체 이력의 취약 태그는 약점도 순으로 최대 네 개까지 제공한다.")
    void weakTags_returnsTopFourInWeaknessOrder() {
        // given
        solveWrongly(
                taggedQuestion("가장취약"),
                taggedQuestion("태그1"),
                taggedQuestion("태그2"),
                taggedQuestion("태그3"),
                taggedQuestion("태그4")
        );

        // when
        WeakTagsResult result = recommendationService.weakTags(USER_ID);

        // then
        assertThat(result.sampleCount()).isEqualTo(5);
        assertThat(result.tags()).hasSize(4);
        assertThat(result.tags()).extracting(WeakTagResult::tag).startsWith("가장취약");
    }

    @Test
    @DisplayName("풀이 이력이 3건 미만이면 AI를 호출하지 않고 난이도 하 문항을 카테고리별로 고르게 준다.")
    void recommend_coldStart() {
        // given
        saveQuestion(Category.DB, Difficulty.LOW, "DB 하 1");
        saveQuestion(Category.DB, Difficulty.LOW, "DB 하 2");
        saveQuestion(Category.NETWORK, Difficulty.LOW, "네트워크 하 1");
        saveQuestion(Category.OS, Difficulty.LOW, "OS 하 1");

        // when
        RecommendationResult result = recommendationService.recommend(USER_ID);

        // then
        assertThat(result.personalized()).isFalse();
        assertThat(result.generated()).isFalse();
        assertThat(result.questions()).hasSize(3);
        // 한 카테고리에서 몰아 뽑지 않는다.
        assertThat(result.questions()).extracting(RecommendedQuestionResult::category)
                .containsExactlyInAnyOrder(Category.DB, Category.NETWORK, Category.OS);
    }

    @Test
    @DisplayName("약점 프로필이 있으면 취약 주제로 서술형 문항을 생성해 검수 전 상태로 저장한다.")
    void recommend_generatesEssayQuestions() {
        // given
        solveWrongly(taggedQuestion("인덱스"), taggedQuestion("트랜잭션"), taggedQuestion("정규화"));

        // when
        RecommendationResult result = recommendationService.recommend(USER_ID);

        // then
        assertThat(result.personalized()).isTrue();
        assertThat(result.generated()).isTrue();

        List<Question> generated = questionRepository.findAll().stream()
                .filter(Question::isGenerated)
                .toList();
        assertThat(generated).isNotEmpty();
        assertThat(generated).allSatisfy(question -> {
            assertThat(question.getSource()).isEqualTo(QuestionSource.GENERATED);
            // 검수 전이므로 문제은행 목록에는 노출되지 않는다.
            assertThat(question.getReviewStatus()).isEqualTo(QuestionReviewStatus.PENDING);
            assertThat(question.getModelAnswer()).isNotBlank();
            assertThat(question.getGradingCriteria()).hasSizeGreaterThanOrEqualTo(2);
        });
    }

    @Test
    @DisplayName("같은 날 다시 조회하면 생성하지 않고 캐시된 문항을 그대로 준다.")
    void recommend_usesCache() {
        // given
        solveWrongly(taggedQuestion("인덱스"), taggedQuestion("트랜잭션"), taggedQuestion("정규화"));
        RecommendationResult first = recommendationService.recommend(USER_ID);
        long questionCountAfterFirst = questionRepository.count();

        // when
        RecommendationResult second = recommendationService.recommend(USER_ID);

        // then
        assertThat(second.questions()).extracting(RecommendedQuestionResult::id)
                .isEqualTo(first.questions().stream().map(RecommendedQuestionResult::id).toList());
        assertThat(questionRepository.count()).isEqualTo(questionCountAfterFirst);
    }

    @Test
    @DisplayName("생성 문항이 검증을 통과하지 못하면 기존 문제은행 문항으로 채운다.")
    void recommend_fallsBackToExistingQuestions() {
        // given
        // 태그 사전이 비어 있어 생성 결과가 태그 규칙을 만족할 수 없다.
        Question first = saveQuestion(Category.DB, Difficulty.MEDIUM, "DB 중 1");
        Question second = saveQuestion(Category.DB, Difficulty.MEDIUM, "DB 중 2");
        Question third = saveQuestion(Category.DB, Difficulty.MEDIUM, "DB 중 3");
        solveWrongly(first, second, third);

        // when
        RecommendationResult result = recommendationService.recommend(USER_ID);

        // then
        assertThat(result.personalized()).isTrue();
        assertThat(result.generated()).isFalse();
        assertThat(result.questions()).isNotEmpty();
        assertThat(result.questions()).allSatisfy(question ->
                assertThat(question.generated()).isFalse());
    }

    @Test
    @DisplayName("추천 문항에는 태그가 함께 내려간다.")
    void recommend_includesTags() {
        // given
        solveWrongly(taggedQuestion("인덱스"), taggedQuestion("트랜잭션"), taggedQuestion("정규화"));

        // when
        RecommendationResult result = recommendationService.recommend(USER_ID);

        // then
        assertThat(result.questions()).anySatisfy(question -> assertThat(question.tags()).isNotEmpty());
    }

    private Question taggedQuestion(String tagName) {
        Question question = saveQuestion(Category.DB, Difficulty.MEDIUM, "DB " + tagName);
        Tag tag = tagRepository.findByNameIn(List.of(tagName)).stream()
                .findFirst()
                .orElseGet(() -> tagRepository.save(TagFixture.db(tagName)));
        questionTagRepository.save(QuestionTag.create(question.getId(), tag.getId()));
        return question;
    }

    private Question saveQuestion(Category category, Difficulty difficulty, String title) {
        return questionRepository.save(QuestionFixture.builder()
                .category(category)
                .difficulty(difficulty)
                .title(title)
                .build());
    }

    private void solveWrongly(Question... questions) {
        for (Question question : questions) {
            solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                    .userId(USER_ID)
                    .questionId(question.getId())
                    .isCorrect(false)
                    .elapsedSeconds(300)
                    .build());
        }
    }
}
