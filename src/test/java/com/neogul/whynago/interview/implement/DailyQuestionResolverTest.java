package com.neogul.whynago.interview.implement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.interview.domain.DailyInterviewQuestion;
import com.neogul.whynago.interview.exception.InterviewErrorCode;
import com.neogul.whynago.interview.infra.DailyInterviewQuestionRepository;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DailyQuestionResolverTest extends IntegrationTestSupport {

    private static final LocalDate TODAY = LocalDate.of(2026, 8, 7);

    @Autowired
    private DailyQuestionResolver dailyQuestionResolver;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private DailyInterviewQuestionRepository dailyInterviewQuestionRepository;

    @Test
    @DisplayName("같은 날 다시 물으면 처음 고정된 질문을 그대로 돌려준다.")
    void resolveReturnsPinnedQuestion() {
        saveEssayQuestions(5);

        Question first = dailyQuestionResolver.resolve(TODAY);
        Question second = dailyQuestionResolver.resolve(TODAY);

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(dailyInterviewQuestionRepository.findById(TODAY).orElseThrow().getQuestionId())
                .isEqualTo(first.getId());
    }

    @Test
    @DisplayName("이미 고정된 질문이 있으면 그 질문을 돌려준다.")
    void resolveWithExistingPin() {
        List<Question> questions = saveEssayQuestions(3);
        Question pinned = questions.get(2);
        dailyInterviewQuestionRepository.save(DailyInterviewQuestion.pin(TODAY, pinned.getId()));

        Question resolved = dailyQuestionResolver.resolve(TODAY);

        assertThat(resolved.getId()).isEqualTo(pinned.getId());
    }

    @Test
    @DisplayName("날짜가 다르면 질문을 새로 고정한다.")
    void resolveForAnotherDate() {
        saveEssayQuestions(3);

        dailyQuestionResolver.resolve(TODAY);
        dailyQuestionResolver.resolve(TODAY.plusDays(1));

        assertThat(dailyInterviewQuestionRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("서술형 문제가 하나도 없으면 예외가 발생한다.")
    void resolveWithoutEssayQuestion() {
        questionRepository.save(QuestionFixture.rootMultipleChoice());

        assertThatThrownBy(() -> dailyQuestionResolver.resolve(TODAY))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(InterviewErrorCode.INTERVIEW_QUESTION_NOT_AVAILABLE));
    }

    @Test
    @DisplayName("객관식 문제는 면접 질문 후보에 오르지 않는다.")
    void resolvePicksOnlyEssay() {
        questionRepository.save(QuestionFixture.rootMultipleChoice());
        questionRepository.save(QuestionFixture.followupMultipleChoice());
        saveEssayQuestions(1);

        Question resolved = dailyQuestionResolver.resolve(TODAY);

        assertThat(resolved.getType()).isEqualTo(QuestionType.ESSAY);
    }

    private List<Question> saveEssayQuestions(int count) {
        return IntStream.range(0, count)
                .mapToObj(i -> questionRepository.save(QuestionFixture.essayRoot()))
                .toList();
    }
}
