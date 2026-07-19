package com.neogul.whynago.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.service.dto.ChoiceGradingResult;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.support.IntegrationTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class QuestionServiceTest extends IntegrationTestSupport {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Test
    @DisplayName("정답 보기를 고르면 정답으로 채점되고 고른 보기에 연결된 꼬리질문을 함께 반환한다.")
    void getChoiceGrading() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        AnswerChoice correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice followupCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup.getId(), 1, null));
        questionTagRepository.save(QuestionTag.create(followup.getId(), "UDP"));

        ChoiceGradingResult result = questionService.getChoiceGrading(root.getId(), correct.getId());

        assertThat(result.correct()).isTrue();
        assertThat(result.correctChoiceId()).isEqualTo(correct.getId());
        assertThat(result.explanation()).isEqualTo(root.getExplanation());
        assertThat(result.choiceExplanation()).isNull();
        assertThat(result.nextQuestion().id()).isEqualTo(followup.getId());
        assertThat(result.nextQuestion().choices())
                .extracting(ChoiceResult::id)
                .containsExactly(followupCorrect.getId());
        assertThat(result.nextQuestion().tags()).containsExactly("UDP");
    }

    @Test
    @DisplayName("오답 보기를 고르면 오답으로 채점되고 정답 보기와 오답 해설을 함께 반환한다.")
    void getChoiceGrading_wrongChoice() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        AnswerChoice wrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));

        ChoiceGradingResult result = questionService.getChoiceGrading(root.getId(), wrong.getId());

        assertThat(result.correct()).isFalse();
        assertThat(result.correctChoiceId()).isEqualTo(correct.getId());
        assertThat(result.explanation()).isEqualTo(root.getExplanation());
        assertThat(result.choiceExplanation()).isEqualTo(wrong.getExplanation());
    }

    @Test
    @DisplayName("고른 보기에 연결된 꼬리질문이 없으면 꼬리질문 없이 반환한다.")
    void getChoiceGrading_noRelatedQuestion() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));

        ChoiceGradingResult result = questionService.getChoiceGrading(root.getId(), correct.getId());

        assertThat(result.correct()).isTrue();
        assertThat(result.nextQuestion()).isNull();
    }

    @Test
    @DisplayName("보기가 해당 문제에 속하지 않으면 예외가 발생한다.")
    void getChoiceGrading_choiceNotInQuestion() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question other = questionRepository.save(QuestionFixture.followupMultipleChoice());
        answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        AnswerChoice otherChoice = answerChoiceRepository.save(AnswerChoiceFixture.correct(other.getId(), 1, null));

        assertThatThrownBy(() -> questionService.getChoiceGrading(root.getId(), otherChoice.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.CHOICE_NOT_IN_QUESTION));
    }

    @Test
    @DisplayName("문제가 존재하지 않으면 예외가 발생한다.")
    void getChoiceGrading_questionNotFound() {
        assertThatThrownBy(() -> questionService.getChoiceGrading(Long.MAX_VALUE, 1L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }
}
