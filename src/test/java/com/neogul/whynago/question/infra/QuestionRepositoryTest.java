package com.neogul.whynago.question.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.dto.CategoryQuestionCount;
import com.neogul.whynago.question.infra.dto.QuestionTagName;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

class QuestionRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("객관식 문제를 필터링해 조회한다.")
    void findQuestions() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        questionRepository.save(QuestionFixture.essayRoot());
        // followup은 root 선택지의 꼬리질문으로도 참조되지만, 그 자체로 독립된 문항이라 조회 대상에서 제외되지 않는다.
        answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        Tag tag = tagRepository.save(TagFixture.of("TCP", Category.NETWORK));
        questionTagRepository.save(QuestionTag.create(root.getId(), tag.getId()));

        Page<Question> result = questionRepository.findQuestions(
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                Category.NETWORK,
                "UDP",
                PageRequest.of(0, 20)
        );

        assertThat(result.getContent()).extracting(Question::getId).containsExactly(followup.getId(), root.getId());
        // 태그 이름은 tag 테이블에 있으므로 조인 조회로 확인한다.
        assertThat(questionTagRepository.findTagNames(List.of(root.getId())))
                .extracting(QuestionTagName::getName)
                .containsExactly("TCP");
    }

    @Test
    @DisplayName("문제 목록에는 검수를 통과하지 않은 문항이 포함되지 않는다.")
    void findQuestions_excludesUnreviewed() {
        Question seeded = questionRepository.save(QuestionFixture.essayRoot());
        questionRepository.save(QuestionFixture.generatedEssay());
        questionRepository.save(QuestionFixture.rejectedGeneratedEssay());

        Page<Question> result = questionRepository.findQuestions(null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting(Question::getId).containsExactly(seeded.getId());
        // countQuery에 필터를 빠뜨리면 행은 맞고 총 개수만 틀린다.
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("승인된 생성 문항은 시드 문항과 함께 목록에 노출된다.")
    void findQuestions_includesApprovedGenerated() {
        Question seeded = questionRepository.save(QuestionFixture.essayRoot());
        // 판별 기준이 source라면 승인해도 계속 빠진다. 노출 게이트는 reviewStatus다.
        Question approved = questionRepository.save(QuestionFixture.approvedGeneratedEssay());

        Page<Question> result = questionRepository.findQuestions(null, null, null, null, PageRequest.of(0, 20));

        assertThat(result.getContent())
                .extracting(Question::getId)
                .containsExactly(approved.getId(), seeded.getId());
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    @DisplayName("단건 조회는 검수 전 문항도 반환한다.")
    void findById_returnsPendingQuestion() {
        Question pending = questionRepository.save(QuestionFixture.generatedEssay());

        // 추천으로 받은 문항을 열 수 있어야 하므로 목록과 달리 단건은 막지 않는다.
        assertThat(questionRepository.findById(pending.getId())).isPresent();
    }

    @Test
    @DisplayName("카테고리별 문제 수에도 검수를 통과하지 않은 문항은 세지 않는다.")
    void countGroupByCategory_excludesUnreviewed() {
        questionRepository.save(QuestionFixture.essayRoot());
        questionRepository.save(QuestionFixture.generatedEssay());
        questionRepository.save(QuestionFixture.rejectedGeneratedEssay());

        List<CategoryQuestionCount> counts = questionRepository.countGroupByCategory();

        assertThat(counts)
                .filteredOn(count -> count.getCategory() == Category.DB)
                .singleElement()
                .satisfies(count -> assertThat(count.getTotal()).isEqualTo(1));
    }

    @Test
    @DisplayName("카테고리별 문제 수에는 승인된 생성 문항이 포함된다.")
    void countGroupByCategory_includesApprovedGenerated() {
        questionRepository.save(QuestionFixture.essayRoot());
        questionRepository.save(QuestionFixture.approvedGeneratedEssay());

        List<CategoryQuestionCount> counts = questionRepository.countGroupByCategory();

        assertThat(counts)
                .filteredOn(count -> count.getCategory() == Category.DB)
                .singleElement()
                .satisfies(count -> assertThat(count.getTotal()).isEqualTo(2));
    }

    @Test
    @DisplayName("유형을 지정하지 않으면 객관식 문제(꼬리질문 포함)와 서술형 문제를 함께 조회한다.")
    void findQuestions_withoutType() {
        // given
        Question multipleChoiceRoot = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        answerChoiceRepository.save(AnswerChoiceFixture.correct(multipleChoiceRoot.getId(), 1, followup.getId()));

        // when
        Page<Question> result = questionRepository.findQuestions(null, null, null, null, PageRequest.of(0, 20));

        // then
        // 꼬리질문으로 참조되는 문항도 그 자체로 독립된 문항이라 목록에서 제외되지 않는다.
        assertThat(result.getContent()).extracting(Question::getId)
                .containsExactlyInAnyOrder(multipleChoiceRoot.getId(), followup.getId(), essay.getId());
    }

    @Test
    @DisplayName("유형을 서술형으로 지정하면 서술형 문제만 조회한다.")
    void findQuestions_essayType() {
        // given
        questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question essay = questionRepository.save(QuestionFixture.essayRoot());

        // when
        Page<Question> result = questionRepository.findQuestions(
                QuestionType.ESSAY,
                null,
                null,
                null,
                PageRequest.of(0, 20)
        );

        // then
        assertThat(result.getContent()).extracting(Question::getId).containsExactly(essay.getId());
        assertThat(result.getContent()).extracting(Question::getType).containsOnly(QuestionType.ESSAY);
    }

    @Test
    @DisplayName("요청한 페이지 크기만큼만 조회하고 조건에 맞는 전체 문항 수를 함께 반환한다.")
    void findQuestions_paged() {
        // given
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question essay = questionRepository.save(QuestionFixture.essayRoot());

        // when
        Page<Question> firstPage = questionRepository.findQuestions(null, null, null, null, PageRequest.of(0, 2));
        Page<Question> lastPage = questionRepository.findQuestions(null, null, null, null, PageRequest.of(1, 2));

        // then
        assertThat(firstPage.getTotalElements()).isEqualTo(3);
        assertThat(firstPage.getContent()).extracting(Question::getId)
                .containsExactly(essay.getId(), followup.getId());
        assertThat(lastPage.getContent()).extracting(Question::getId).containsExactly(root.getId());
    }

    @Test
    @DisplayName("조회 범위를 벗어난 페이지를 요청하면 빈 목록과 전체 문항 수를 반환한다.")
    void findQuestions_pageOutOfRange() {
        // given
        questionRepository.save(QuestionFixture.rootMultipleChoice());

        // when
        Page<Question> result = questionRepository.findQuestions(null, null, null, null, PageRequest.of(5, 20));

        // then
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("카테고리별 전체 문항 수를 세고, 문항이 없는 카테고리는 결과에 없다.")
    void countGroupByCategory() {
        // given — NETWORK 2개(꼬리질문 포함), DB 1개
        questionRepository.save(QuestionFixture.rootMultipleChoice());
        questionRepository.save(QuestionFixture.followupMultipleChoice());
        questionRepository.save(QuestionFixture.essayRoot());

        // when
        List<CategoryQuestionCount> result = questionRepository.countGroupByCategory();

        // then
        assertThat(result)
                .extracting(CategoryQuestionCount::getCategory, CategoryQuestionCount::getTotal)
                .containsExactlyInAnyOrder(
                        tuple(Category.NETWORK, 2L),
                        tuple(Category.DB, 1L)
                );
    }

    @Test
    @DisplayName("문제의 선택지를 순서대로 조회하고 정답 선택지를 조회한다.")
    void findChoices() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice second = answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice first = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));

        List<AnswerChoice> choices = answerChoiceRepository.findByQuestionIdOrderBySequence(root.getId());

        assertThat(choices).extracting(AnswerChoice::getId).containsExactly(first.getId(), second.getId());
        assertThat(answerChoiceRepository.findFirstByQuestionIdAndIsCorrectTrue(root.getId()))
                .get()
                .extracting(AnswerChoice::getId)
                .isEqualTo(first.getId());
    }
}
