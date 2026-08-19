package com.neogul.whynago.question.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.AnswerChoiceFixture;

import com.neogul.whynago.fixture.EssaySolvedFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.fixture.SolvedMultipleChoiceFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.question.infra.TagRepository;
import com.neogul.whynago.question.service.dto.ChoiceGradingResult;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.question.service.dto.EssayQuestionResult;
import com.neogul.whynago.question.service.dto.QuestionResult;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import com.neogul.whynago.question.service.dto.QuestionsResult;
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

class QuestionServiceTest extends IntegrationTestSupport {

    @Autowired
    private QuestionService questionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private QuestionTagRepository questionTagRepository;

    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("정답 보기를 고르면 정답으로 채점되고 고른 보기에 연결된 꼬리질문을 함께 반환한다.")
    void getChoiceGrading() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        AnswerChoice correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice followupCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup.getId(), 1, null));
        saveTag(followup.getId(), "UDP", Category.NETWORK);

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

    @Test
    @DisplayName("문제 목록에 서술형 문제가 선택지 없이 태그와 함께 포함된다.")
    void findQuestions_essay() {
        // given
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        saveTag(essay.getId(), "트랜잭션", Category.DB);

        // when
        QuestionsResult result = questionService.findQuestions(
                10L,
                QuestionSearchCommand.of(QuestionType.ESSAY, null, null, null, null, null)
        );

        // then
        List<QuestionResult> results = result.questions();
        assertThat(results).hasSize(1);
        assertThat(results.getFirst().id()).isEqualTo(essay.getId());
        assertThat(results.getFirst().type()).isEqualTo(QuestionType.ESSAY);
        assertThat(results.getFirst().choices()).isEmpty();
        assertThat(results.getFirst().tags()).containsExactly("트랜잭션");
    }

    @Test
    @DisplayName("이미 푼 문제는 solved가 true이고 풀지 않은 문제는 false다.")
    void findQuestions_solved() {
        // given
        Question solvedQuestion = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question unsolvedQuestion = questionRepository.save(QuestionFixture.followupMultipleChoice());
        solvedMultipleChoiceRepository.save(solvedMultipleChoice(10L, solvedQuestion.getId()));

        // when
        QuestionsResult result = questionService.findQuestions(
                10L,
                QuestionSearchCommand.of(QuestionType.MULTIPLE_CHOICE, null, null, null, null, null)
        );

        // then
        assertThat(result.questions())
                .extracting(QuestionResult::id, QuestionResult::solved)
                .containsExactlyInAnyOrder(
                        tuple(solvedQuestion.getId(), true),
                        tuple(unsolvedQuestion.getId(), false)
                );
    }

    @Test
    @DisplayName("서술형으로 푼 문제도 solved가 true다.")
    void findQuestions_solvedByEssay() {
        // given
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        essaySolvedRepository.save(essaySolved(10L, essay.getId()));

        // when
        QuestionsResult result = questionService.findQuestions(
                10L,
                QuestionSearchCommand.of(QuestionType.ESSAY, null, null, null, null, null)
        );

        // then
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().getFirst().solved()).isTrue();
    }

    @Test
    @DisplayName("다른 사용자가 푼 문제는 solved가 false다.")
    void findQuestions_solvedByOtherUser() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        solvedMultipleChoiceRepository.save(solvedMultipleChoice(20L, question.getId()));

        // when
        QuestionsResult result = questionService.findQuestions(
                10L,
                QuestionSearchCommand.of(QuestionType.MULTIPLE_CHOICE, null, null, null, null, null)
        );

        // then
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().getFirst().solved()).isFalse();
    }

    @Test
    @DisplayName("비로그인으로 조회하면 푼 문제여도 solved가 false다.")
    void findQuestions_withoutUser() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        solvedMultipleChoiceRepository.save(solvedMultipleChoice(10L, question.getId()));

        // when
        QuestionsResult result = questionService.findQuestions(
                null,
                QuestionSearchCommand.of(QuestionType.MULTIPLE_CHOICE, null, null, null, null, null)
        );

        // then
        assertThat(result.questions()).hasSize(1);
        assertThat(result.questions().getFirst().solved()).isFalse();
    }

    @Test
    @DisplayName("요청한 페이지의 문제만 반환하고 전체 문항 수를 함께 반환한다.")
    void findQuestions_paged() {
        // given
        questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question essay = questionRepository.save(QuestionFixture.essayRoot());

        // when
        QuestionsResult result = questionService.findQuestions(
                10L,
                QuestionSearchCommand.of(null, null, null, null, 0, 2)
        );

        // then
        assertThat(result.questions()).extracting(QuestionResult::id)
                .containsExactly(essay.getId(), followup.getId());
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(2);
        assertThat(result.totalElements()).isEqualTo(3);
    }

    @Test
    @DisplayName("페이지 정보를 주지 않으면 첫 페이지를 20개 크기로 조회한다.")
    void findQuestions_defaultPaging() {
        // given
        questionRepository.save(QuestionFixture.rootMultipleChoice());

        // when
        QuestionsResult result = questionService.findQuestions(
                10L,
                QuestionSearchCommand.of(null, null, null, null, null, null)
        );

        // then
        assertThat(result.page()).isZero();
        assertThat(result.size()).isEqualTo(20);
        assertThat(result.totalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("문제를 단건 조회하면 선택지·태그와 푼 문제 여부를 함께 반환한다.")
    void findQuestion() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(question.getId(), 1, null));
        saveTag(question.getId(), "TCP", Category.NETWORK);
        solvedMultipleChoiceRepository.save(solvedMultipleChoice(10L, question.getId()));

        // when
        QuestionResult result = questionService.findQuestion(10L, question.getId());

        // then
        assertThat(result.id()).isEqualTo(question.getId());
        assertThat(result.choices()).extracting(ChoiceResult::id).containsExactly(choice.getId());
        assertThat(result.tags()).containsExactly("TCP");
        assertThat(result.solved()).isTrue();
    }

    @Test
    @DisplayName("비로그인으로 문제를 단건 조회하면 푼 문제여도 solved가 false다.")
    void findQuestion_withoutUser() {
        // given
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        solvedMultipleChoiceRepository.save(solvedMultipleChoice(10L, question.getId()));

        // when
        QuestionResult result = questionService.findQuestion(null, question.getId());

        // then
        assertThat(result.solved()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 문제를 단건 조회하면 예외가 발생한다.")
    void findQuestion_questionNotFound() {
        // when & then
        assertThatThrownBy(() -> questionService.findQuestion(10L, Long.MAX_VALUE))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    @Test
    @DisplayName("서술형 본 질문을 조회하면 문항 정보와 태그를 반환한다.")
    void findEssayQuestion() {
        // given
        Question essay = questionRepository.save(QuestionFixture.essayRoot());
        saveTag(essay.getId(), "트랜잭션", Category.DB);
        saveTag(essay.getId(), "격리 수준", Category.DB);

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

    private SolvedMultipleChoice solvedMultipleChoice(Long userId, Long questionId) {
        return SolvedMultipleChoiceFixture.builder()
                .userId(userId)
                .questionId(questionId)
                .build();
    }

    private EssaySolved essaySolved(Long userId, Long questionId) {
        return EssaySolvedFixture.builder()
                .userId(userId)
                .questionId(questionId)
                .build();
    }

    // 태그 이름은 tag 테이블에 있고 question_tag는 그 id를 참조한다.
    private void saveTag(Long questionId, String name, Category category) {
        Tag tag = tagRepository.save(TagFixture.of(name, category));
        questionTagRepository.save(QuestionTag.create(questionId, tag.getId()));
    }
}
