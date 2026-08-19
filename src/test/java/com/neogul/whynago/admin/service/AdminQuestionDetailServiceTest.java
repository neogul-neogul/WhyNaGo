package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.neogul.whynago.admin.service.dto.AdminChoiceResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionDetailResult;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.infra.TagRepository;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminQuestionDetailServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminQuestionDetailService adminQuestionDetailService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("객관식 문제 상세를 조회하면 선택지에 정답 여부가 그대로 노출된다.")
    void readQuestion_multipleChoice() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(question.getId(), 1, null));
        AnswerChoice wrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(question.getId(), 2));
        Tag tag = tagRepository.save(TagFixture.of("TCP/IP", Category.NETWORK));
        questionTagRepository.save(QuestionTag.create(question.getId(), tag.getId()));

        // when
        AdminQuestionDetailResult result = adminQuestionDetailService.readQuestion(question.getId());

        // then
        assertThat(result.id()).isEqualTo(question.getId());
        assertThat(result.tags()).containsExactly("TCP/IP");
        assertThat(result.choices())
                .extracting(AdminChoiceResult::id, AdminChoiceResult::correct)
                .containsExactlyInAnyOrder(
                        tuple(correct.getId(), true),
                        tuple(wrong.getId(), false)
                );
    }

    @Test
    @DisplayName("객관식 문제 상세의 풀이수·정답률은 항상 null이다 — 별도 통계 API를 쓴다.")
    void readQuestion_multipleChoice_hasNoSolveStats() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        answerChoiceRepository.save(AnswerChoiceFixture.correct(question.getId(), 1, null));

        // when
        AdminQuestionDetailResult result = adminQuestionDetailService.readQuestion(question.getId());

        // then
        assertThat(result.solveCount()).isNull();
        assertThat(result.correctRate()).isNull();
    }

    @Test
    @DisplayName("서술형 문제 상세를 조회하면 선택지는 비어 있고 풀이수·정답률이 함께 내려온다.")
    void readQuestion_essay() {
        // given
        Question question = questionRepository.save(QuestionFixture.essayRoot());
        solveEssay(question.getId(), true);
        solveEssay(question.getId(), true);
        solveEssay(question.getId(), false);

        // when
        AdminQuestionDetailResult result = adminQuestionDetailService.readQuestion(question.getId());

        // then
        assertThat(result.choices()).isEmpty();
        assertThat(result.solveCount()).isEqualTo(3);
        assertThat(result.correctRate()).isEqualTo(66.7);
    }

    @Test
    @DisplayName("아직 아무도 풀지 않은 서술형 문제는 풀이수가 0이고 정답률이 없다.")
    void readQuestion_essay_noRecord() {
        // given
        Question question = questionRepository.save(QuestionFixture.essayRoot());

        // when
        AdminQuestionDetailResult result = adminQuestionDetailService.readQuestion(question.getId());

        // then
        assertThat(result.solveCount()).isZero();
        assertThat(result.correctRate()).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 문제를 조회하면 예외가 발생한다.")
    void readQuestion_notFound() {
        // when & then
        assertThatThrownBy(() -> adminQuestionDetailService.readQuestion(999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(e -> assertThat(((BusinessException) e).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    private void solveEssay(Long questionId, boolean isCorrect) {
        essaySolvedRepository.save(EssaySolved.create(
                1L,
                10L,
                ItemType.MAIN,
                1,
                questionId,
                "발문",
                "답변",
                "피드백",
                "모범답안",
                isCorrect,
                null,
                null,
                LocalDateTime.now()
        ));
    }
}
