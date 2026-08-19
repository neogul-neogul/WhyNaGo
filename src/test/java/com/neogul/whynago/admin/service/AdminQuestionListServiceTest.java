package com.neogul.whynago.admin.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.admin.service.dto.AdminQuestionResult;
import com.neogul.whynago.admin.service.dto.AdminQuestionsResult;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionReviewStatus;
import com.neogul.whynago.question.domain.QuestionSource;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class AdminQuestionListServiceTest extends IntegrationTestSupport {

    @Autowired
    private AdminQuestionListService adminQuestionListService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("객관식 문제의 풀이수·정답률을 함께 조회한다.")
    void readQuestions_multipleChoice() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(question.getId(), 1, null));
        AnswerChoice wrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(question.getId(), 2));
        solveMultipleChoice(question.getId(), correct.getId(), true);
        solveMultipleChoice(question.getId(), correct.getId(), true);
        solveMultipleChoice(question.getId(), wrong.getId(), false);

        // when
        AdminQuestionsResult result = adminQuestionListService.readQuestions(searchCommand());

        // then
        AdminQuestionResult found = findById(result, question.getId());
        assertThat(found.solveCount()).isEqualTo(3);
        assertThat(found.correctRate()).isEqualTo(66.7);
    }

    @Test
    @DisplayName("서술형 문제의 풀이수·정답률을 함께 조회한다.")
    void readQuestions_essay() {
        // given
        Question question = questionRepository.save(QuestionFixture.essayRoot());
        solveEssay(question.getId(), true);
        solveEssay(question.getId(), true);
        solveEssay(question.getId(), false);
        solveEssay(question.getId(), false);

        // when
        AdminQuestionsResult result = adminQuestionListService.readQuestions(searchCommand());

        // then
        AdminQuestionResult found = findById(result, question.getId());
        assertThat(found.solveCount()).isEqualTo(4);
        assertThat(found.correctRate()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("아직 아무도 풀지 않은 문제는 풀이수가 0이고 정답률이 없다.")
    void readQuestions_noRecord() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        answerChoiceRepository.save(AnswerChoiceFixture.correct(question.getId(), 1, null));

        // when
        AdminQuestionsResult result = adminQuestionListService.readQuestions(searchCommand());

        // then
        AdminQuestionResult found = findById(result, question.getId());
        assertThat(found.solveCount()).isZero();
        assertThat(found.correctRate()).isNull();
    }

    @Test
    @DisplayName("문제 유형으로 필터링해 조회한다.")
    void readQuestions_filterByType() {
        // given
        Question multipleChoice = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question essay = questionRepository.save(QuestionFixture.essayRoot());

        // when
        AdminQuestionsResult result = adminQuestionListService.readQuestions(
                QuestionSearchCommand.of(QuestionType.ESSAY, null, null, null, null, null));

        // then
        assertThat(result.questions()).extracting(AdminQuestionResult::type).containsOnly(QuestionType.ESSAY);
        assertThat(result.questions()).extracting(AdminQuestionResult::id)
                .contains(essay.getId())
                .doesNotContain(multipleChoice.getId());
    }

    private AdminQuestionResult findById(AdminQuestionsResult result, Long questionId) {
        return result.questions().stream()
                .filter(question -> question.id().equals(questionId))
                .findFirst()
                .orElseThrow();
    }

    @Test
    @DisplayName("검수 전 생성 문항도 관리자 목록에는 노출한다.")
    void readQuestions_includesPendingQuestion() {
        // given
        // 문제은행 목록(GET /api/questions)은 APPROVED만 보여주지만, 검수 화면은 그 대기열을 봐야 한다.
        Question pending = questionRepository.save(Question.generated(
                "검수 대기 문항",
                "아직 승인되지 않은 생성 문항이다.",
                Difficulty.MEDIUM,
                Category.DB,
                "모범답안",
                List.of("기준1", "기준2")
        ));

        // when
        AdminQuestionsResult result = adminQuestionListService.readQuestions(searchCommand());

        // then
        assertThat(result.questions())
                .filteredOn(question -> question.id().equals(pending.getId()))
                .singleElement()
                .satisfies(question -> {
                    // 목록에 섞여 나가므로 화면이 검수 대기임을 구분할 수 있어야 한다.
                    assertThat(question.reviewStatus()).isEqualTo(QuestionReviewStatus.PENDING);
                    assertThat(question.source()).isEqualTo(QuestionSource.GENERATED);
                });
    }

    private QuestionSearchCommand searchCommand() {
        return QuestionSearchCommand.of(null, null, null, null, null, null);
    }

    private void solveMultipleChoice(Long questionId, Long choiceId, boolean isCorrect) {
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                1L,
                10L,
                questionId,
                ItemType.MAIN,
                1,
                choiceId,
                choiceId,
                isCorrect,
                null,
                LocalDateTime.now()
        ));
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
