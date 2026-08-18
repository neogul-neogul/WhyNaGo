package com.neogul.whynago.progress.implement;

import static org.assertj.core.api.Assertions.assertThat;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.EssaySolvedFixture;

import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.fixture.SolvedMultipleChoiceFixture;
import com.neogul.whynago.progress.implement.dto.UserProgressAggregate;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class UserScoreCalculatorTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;
    private static final LocalDateTime SOLVED_AT = LocalDateTime.of(2026, 6, 25, 10, 0);

    @Autowired
    private UserScoreCalculator userScoreCalculator;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Test
    @DisplayName("본질문과 꼬리질문을 모두 맞힌 객관식 세션은 난이도 점수를 지급한다.")
    void calculate_multipleChoiceFullyCorrect() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveMultipleChoiceSession(root, 1, 1);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(2);
        assertThat(result.totalQuestionCount()).isEqualTo(1);
        assertThat(result.totalCorrectCount()).isEqualTo(1);
        assertThat(result.categoryQuestionCounts()).containsEntry(Category.NETWORK, 1);
        assertThat(result.categoryCorrectCounts()).containsEntry(Category.NETWORK, 1);
        assertThat(result.categoryScores()).containsEntry(Category.NETWORK, 2);
    }

    @Test
    @DisplayName("꼬리질문까지 모두 맞히지 못하면 점수를 받지 못한다.")
    void calculate_partiallyCorrect_noScore() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveMultipleChoiceSession(root, 2, 1);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isZero();
        assertThat(result.categoryQuestionCounts()).containsEntry(Category.NETWORK, 1);
        assertThat(result.categoryCorrectCounts()).isEmpty();
        assertThat(result.categoryScores()).isEmpty();
    }

    @Test
    @DisplayName("같은 본질문을 다시 풀어 만점을 받아도 두 번째부터는 점수를 받지 못한다.")
    void calculate_sameQuestionSolvedTwice_scoredOnce() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveMultipleChoiceSession(root, 1, 1);
        saveMultipleChoiceSession(root, 1, 1);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(2);
        assertThat(result.totalQuestionCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("최초 시도가 오답이어도 이후 같은 본질문에서 만점을 받으면 점수를 지급한다.")
    void calculate_firstAttemptWrong_thenFullyCorrect_scored() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        saveMultipleChoiceSession(root, 1, 0);
        saveMultipleChoiceSession(root, 1, 1);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(2);
    }

    @Test
    @DisplayName("객관식 꼬리질문은 본질문과 별개로 자기 난이도만큼 점수를 받는다.")
    void calculate_followupScoredByOwnDifficulty() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice()); // MEDIUM = 2점
        Question followup = questionRepository.save(Question.create(
                "실시간 음성 통화와 UDP", "실시간 음성 통화에 UDP가 적합한 가장 큰 이유는?",
                QuestionType.MULTIPLE_CHOICE, Difficulty.HIGH, Category.NETWORK, "낮은 지연이 중요하다.")); // HIGH = 3점
        saveMultipleChoiceChain(List.of(root, followup), 2);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(5);
        assertThat(result.categoryQuestionCounts()).containsEntry(Category.NETWORK, 2);
    }

    @Test
    @DisplayName("한 세션에서 꼬리질문으로 이미 점수를 받은 문항은 다른 세션에서 본질문으로 다시 나와도 점수를 받지 못한다.")
    void calculate_sameQuestionAcrossSessionsInDifferentRoles_scoredOnce() {
        Question q1 = questionRepository.save(QuestionFixture.rootMultipleChoice()); // MEDIUM = 2점
        Question q2 = questionRepository.save(Question.create(
                "q2", "q2 content", QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM, Category.NETWORK, ""));
        Question q3 = questionRepository.save(Question.create(
                "q3", "q3 content", QuestionType.MULTIPLE_CHOICE, Difficulty.MEDIUM, Category.NETWORK, ""));
        saveMultipleChoiceChain(List.of(q1, q2), 2); // 세션1: q1(본)->q2(꼬리) 모두 정답
        saveMultipleChoiceChain(List.of(q2, q3), 2); // 세션2: q2(본)->q3(꼬리) 모두 정답, q2는 이미 지급됨

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(6); // q1 + q2 + q3, 각 2점씩 딱 한 번
    }

    @Test
    @DisplayName("서술형은 난이도 점수의 4배를 지급한다.")
    void calculate_essayFullyCorrect() {
        Question root = questionRepository.save(QuestionFixture.essayRoot());
        saveEssaySession(root, true);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(12);
        assertThat(result.categoryQuestionCounts()).containsEntry(Category.DB, 1);
        assertThat(result.categoryScores()).containsEntry(Category.DB, 12);
    }

    @Test
    @DisplayName("카테고리가 여러 개면 점수를 카테고리별로 나눠 집계한다.")
    void calculate_scoresGroupedByCategory() {
        Question network = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question db = questionRepository.save(Question.create(
                "인덱스", "인덱스 설명", QuestionType.MULTIPLE_CHOICE, Difficulty.HIGH, Category.DB, ""));
        saveMultipleChoiceChain(List.of(network), 1);
        saveMultipleChoiceChain(List.of(db), 1);

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isEqualTo(5);
        assertThat(result.categoryScores())
                .containsEntry(Category.NETWORK, 2)
                .containsEntry(Category.DB, 3);
        assertThat(result.categoryCorrectCounts())
                .containsEntry(Category.NETWORK, 1)
                .containsEntry(Category.DB, 1);
    }

    @Test
    @DisplayName("다른 사용자의 풀이 기록은 점수·집계에 포함하지 않는다.")
    void calculate_onlyOwnRecords() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Long otherUserId = 999L;
        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(otherUserId, QuestionType.MULTIPLE_CHOICE, 1, 1, SOLVED_AT.minusMinutes(5), SOLVED_AT));
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .solvedSessionId(session.getId()).userId(otherUserId).questionId(root.getId())
                .userChoiceId(choice.getId()).answerChoiceId(choice.getId()).solvedAt(SOLVED_AT)
                .build());

        UserProgressAggregate result = userScoreCalculator.calculate(USER_ID);

        assertThat(result.totalScore()).isZero();
        assertThat(result.totalQuestionCount()).isZero();
        assertThat(result.categoryQuestionCounts()).isEmpty();
        assertThat(result.categoryScores()).isEmpty();
    }

    private void saveMultipleChoiceSession(Question root, int totalCount, int correctCount) {
        SolvedSession session = solvedSessionRepository.save(SolvedSession.completed(
                USER_ID, QuestionType.MULTIPLE_CHOICE, totalCount, correctCount, SOLVED_AT.minusMinutes(5), SOLVED_AT));
        AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, null));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                .solvedSessionId(session.getId()).userId(USER_ID).questionId(root.getId())
                .userChoiceId(choice.getId()).answerChoiceId(choice.getId()).solvedAt(SOLVED_AT)
                .build());
    }

    // questions: 본질문(첫 번째)부터 꼬리질문까지 세션 내 등장 순서.
    private void saveMultipleChoiceChain(List<Question> questions, int correctCount) {
        SolvedSession session = solvedSessionRepository.save(SolvedSession.completed(
                USER_ID, QuestionType.MULTIPLE_CHOICE, questions.size(), correctCount, SOLVED_AT.minusMinutes(5), SOLVED_AT));
        int sequence = 1;
        for (Question question : questions) {
            ItemType type = sequence == 1 ? ItemType.MAIN : ItemType.FOLLOWUP;
            AnswerChoice choice = answerChoiceRepository.save(AnswerChoiceFixture.correct(question.getId(), 1, null));
            solvedMultipleChoiceRepository.save(SolvedMultipleChoiceFixture.builder()
                    .solvedSessionId(session.getId()).userId(USER_ID).questionId(question.getId())
                    .type(type).sequence(sequence)
                    .userChoiceId(choice.getId()).answerChoiceId(choice.getId()).solvedAt(SOLVED_AT)
                    .build());
            sequence++;
        }
    }

    private void saveEssaySession(Question root, boolean isCorrect) {
        SolvedSession session = solvedSessionRepository.save(SolvedSession.completed(
                USER_ID, QuestionType.ESSAY, 1, isCorrect ? 1 : 0, SOLVED_AT.minusMinutes(5), SOLVED_AT));
        essaySolvedRepository.save(EssaySolvedFixture.builder()
                .solvedSessionId(session.getId()).userId(USER_ID).questionId(root.getId())
                .questionText(root.getContent()).userAnswer("내 답변").isCorrect(isCorrect).solvedAt(SOLVED_AT)
                .build());
    }
}
