package com.neogul.whynago.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.service.dto.EssayQuestionResult;
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
    private QuestionTagRepository questionTagRepository;

    @Test
    @DisplayName("서술형 본 질문을 조회하면 문항 정보와 태그를 반환한다.")
    void findEssayQuestion() {
        // given
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        questionTagRepository.save(QuestionTag.create(essay.getId(), "트랜잭션"));
        questionTagRepository.save(QuestionTag.create(essay.getId(), "격리 수준"));

        // when
        EssayQuestionResult result = questionService.findEssayQuestion(essay.getId());

        // then
        assertThat(result.id()).isEqualTo(essay.getId());
        assertThat(result.type()).isEqualTo(QuestionType.ESSAY);
        assertThat(result.difficulty()).isEqualTo(Difficulty.HIGH);
        assertThat(result.category()).isEqualTo(Category.DB);
        assertThat(result.content()).isEqualTo(essay.getContent());
        assertThat(result.tags()).containsExactlyInAnyOrder("트랜잭션", "격리 수준");
    }

    @Test
    @DisplayName("존재하지 않는 문제를 조회하면 예외가 발생한다.")
    void findEssayQuestion_questionNotFound() {
        // when & then
        assertThatThrownBy(() -> questionService.findEssayQuestion(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    @Test
    @DisplayName("서술형이 아닌 문제를 서술형으로 조회하면 예외가 발생한다.")
    void findEssayQuestion_notEssay() {
        // given
        Question multipleChoice = questionRepository.save(QuestionFixture.rootMultipleChoice());

        // when & then
        assertThatThrownBy(() -> questionService.findEssayQuestion(multipleChoice.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_ESSAY));
    }
}
