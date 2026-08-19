package com.neogul.whynago.question.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class QuestionTest {

    @Test
    @DisplayName("시드 문항은 검수를 통과한 상태로 생성된다.")
    void createIsApproved() {
        Question question = QuestionFixture.essayRoot();

        assertThat(question.getSource()).isEqualTo(QuestionSource.SEEDED);
        assertThat(question.getReviewStatus()).isEqualTo(QuestionReviewStatus.APPROVED);
        assertThat(question.isApproved()).isTrue();
    }

    @Test
    @DisplayName("AI 생성 문항은 검수 전 상태로 생성된다.")
    void generatedIsPending() {
        Question question = QuestionFixture.generatedEssay();

        assertThat(question.getSource()).isEqualTo(QuestionSource.GENERATED);
        assertThat(question.getReviewStatus()).isEqualTo(QuestionReviewStatus.PENDING);
        assertThat(question.isApproved()).isFalse();
    }

    @Test
    @DisplayName("AI 생성 문항은 모범답안과 채점 기준을 함께 보관한다.")
    void generatedKeepsModelAnswerAndCriteria() {
        Question question = QuestionFixture.generatedEssay();

        assertThat(question.getModelAnswer()).isNotBlank();
        assertThat(question.getGradingCriteria()).hasSize(2);
        // 생성 문항은 오답 해설이 없다. 해설 자리를 비워 두는 것이 정상이다.
        assertThat(question.getExplanation()).isNull();
    }

    @Test
    @DisplayName("시드 문항은 모범답안 없이 해설만 갖는다.")
    void createHasNoModelAnswer() {
        Question question = QuestionFixture.essayRoot();

        assertThat(question.getModelAnswer()).isNull();
        assertThat(question.getGradingCriteria()).isEmpty();
        assertThat(question.getExplanation()).isNotBlank();
    }

    @Test
    @DisplayName("승인하면 노출 대상이 되지만 생성 문항이라는 출신은 남는다.")
    void approve() {
        Question question = QuestionFixture.generatedEssay();

        question.approve();

        assertThat(question.isApproved()).isTrue();
        // 추천 자기 참조·통계 오염 대응은 승인 여부가 아니라 출신으로 판단한다.
        assertThat(question.isGenerated()).isTrue();
    }

    @Test
    @DisplayName("거절하면 노출 대상에서 빠진다.")
    void reject() {
        Question question = QuestionFixture.generatedEssay();

        question.reject();

        assertThat(question.getReviewStatus()).isEqualTo(QuestionReviewStatus.REJECTED);
        assertThat(question.isApproved()).isFalse();
    }

    @Test
    @DisplayName("이미 승인된 문항은 다시 검수할 수 없다.")
    void approve_alreadyDecided() {
        Question question = QuestionFixture.approvedGeneratedEssay();

        assertThatThrownBy(question::reject)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_REVIEW_ALREADY_DECIDED));
    }

    @Test
    @DisplayName("이미 거절된 문항은 승인으로 되돌릴 수 없다.")
    void reject_alreadyDecided() {
        Question question = QuestionFixture.rejectedGeneratedEssay();

        assertThatThrownBy(question::approve)
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_REVIEW_ALREADY_DECIDED));
    }

    @Test
    @DisplayName("시드 문항은 이미 승인 상태이므로 검수 전이를 시도할 수 없다.")
    void approve_seededQuestion() {
        Question question = QuestionFixture.essayRoot();

        assertThatThrownBy(question::approve)
                .isInstanceOf(BusinessException.class);
    }
}
