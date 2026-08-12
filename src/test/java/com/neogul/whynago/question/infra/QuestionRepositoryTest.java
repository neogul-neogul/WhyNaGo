package com.neogul.whynago.question.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.dto.CategoryQuestionCount;
import com.neogul.whynago.support.RepositoryTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class QuestionRepositoryTest extends RepositoryTestSupport {

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Test
    @DisplayName("객관식 문제를 필터링해 조회한다.")
    void findQuestions() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        questionRepository.save(QuestionFixture.essayRoot());
        // followup은 root 선택지의 꼬리질문으로도 참조되지만, 그 자체로 독립된 문항이라 조회 대상에서 제외되지 않는다.
        answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        questionTagRepository.save(QuestionTag.create(root.getId(), "NETWORK"));

        List<Question> result = questionRepository.findQuestions(
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                Category.NETWORK,
                "UDP"
        );

        assertThat(result).extracting(Question::getId).containsExactly(followup.getId(), root.getId());
        assertThat(questionTagRepository.findByQuestionIdIn(List.of(root.getId())))
                .extracting(QuestionTag::getName)
                .containsExactly("NETWORK");
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
        List<Question> result = questionRepository.findQuestions(null, null, null, null);

        // then
        // 꼬리질문으로 참조되는 문항도 그 자체로 독립된 문항이라 목록에서 제외되지 않는다.
        assertThat(result).extracting(Question::getId)
                .containsExactlyInAnyOrder(multipleChoiceRoot.getId(), followup.getId(), essay.getId());
    }

    @Test
    @DisplayName("유형을 서술형으로 지정하면 서술형 문제만 조회한다.")
    void findQuestions_essayType() {
        // given
        questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question essay = questionRepository.save(QuestionFixture.essayRoot());

        // when
        List<Question> result = questionRepository.findQuestions(QuestionType.ESSAY, null, null, null);

        // then
        assertThat(result).extracting(Question::getId).containsExactly(essay.getId());
        assertThat(result).extracting(Question::getType).containsOnly(QuestionType.ESSAY);
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
