package com.neogul.whynago.question.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.QuestionType;
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
    @DisplayName("객관식 루트 문제를 필터링해 조회한다.")
    void findRootMultipleChoices() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        questionRepository.save(QuestionFixture.followupMultipleChoice());
        questionRepository.save(QuestionFixture.essayRoot());
        questionTagRepository.save(QuestionTag.create(root.getId(), "NETWORK"));

        List<Question> result = questionRepository.findRootMultipleChoices(
                QuestionType.MULTIPLE_CHOICE,
                Difficulty.MEDIUM,
                Category.NETWORK,
                "TCP"
        );

        assertThat(result).extracting(Question::getId).containsExactly(root.getId());
        assertThat(questionTagRepository.findByQuestionIdIn(List.of(root.getId())))
                .extracting(QuestionTag::getName)
                .containsExactly("NETWORK");
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
