package com.neogul.whynago.solvedsession.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.exception.SolvedSessionErrorCode;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionCommand;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionResult;
import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionCommand;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class SolvedSessionServiceTest extends IntegrationTestSupport {

    @Autowired
    private SolvedSessionService solvedSessionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerChoiceRepository answerChoiceRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    @Autowired
    private WrongNoteRepository wrongNoteRepository;

    @Test
    @DisplayName("본질문과 꼬리질문을 이어 푼 결과를 제출하면 세션과 문항 결과, 오답노트를 저장한다.")
    void create() {
        QuizData quiz = saveQuizData();

        CreateSolvedSessionResult result = solvedSessionService.create(10L, quiz.toCommand());

        SolvedSession savedSession = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(savedSession.getTotalCount()).isEqualTo(3);
        assertThat(savedSession.getCorrectCount()).isEqualTo(2);
        assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(10L, result.sessionId())).isTrue();

        List<SolvedMultipleChoice> items = solvedMultipleChoiceRepository.findBySolvedSessionIdOrderBySequence(result.sessionId());
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getType()).isEqualTo(ItemType.MAIN);
        assertThat(items.get(0).getSequence()).isOne();
        assertThat(items.get(1).getType()).isEqualTo(ItemType.FOLLOWUP);
        assertThat(items.get(2).getType()).isEqualTo(ItemType.FOLLOWUP);
        assertThat(items.get(2).isCorrect()).isFalse();
        assertThat(items.get(2).getAnswerChoiceId()).isEqualTo(quiz.followup2Correct().getId());
    }

    @Test
    @DisplayName("전부 정답이면 오답노트를 만들지 않는다.")
    void createAllCorrect() {
        QuizData quiz = saveQuizData();

        CreateSolvedSessionResult result = solvedSessionService.create(10L, quiz.toAllCorrectCommand());

        SolvedSession savedSession = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(savedSession.getCorrectCount()).isEqualTo(3);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(10L, result.sessionId())).isFalse();
    }

    @Test
    @DisplayName("꼬리질문이 다음 문제와 연결되지 않으면 예외가 발생한다.")
    void createWithBrokenChain() {
        QuizData quiz = saveQuizData();
        CreateSolvedSessionCommand command = new CreateSolvedSessionCommand(
                new SolvedQuestionCommand(quiz.root().getId(), quiz.rootCorrect().getId(), 9999L),
                List.of()
        );

        assertThatThrownBy(() -> solvedSessionService.create(10L, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(SolvedSessionErrorCode.SOLVED_SESSION_BROKEN_CHAIN));
    }

    @Test
    @DisplayName("선택지가 제출 문항에 속하지 않으면 예외가 발생한다.")
    void createWithChoiceNotInQuestion() {
        QuizData quiz = saveQuizData();
        CreateSolvedSessionCommand command = new CreateSolvedSessionCommand(
                new SolvedQuestionCommand(quiz.root().getId(), quiz.followup1Correct().getId(), quiz.followup1().getId()),
                List.of()
        );

        assertThatThrownBy(() -> solvedSessionService.create(10L, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.CHOICE_NOT_IN_QUESTION));
    }

    private QuizData saveQuizData() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup1 = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question followup2 = questionRepository.save(QuestionFixture.followupMultipleChoice());

        AnswerChoice rootCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup1.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice followup1Correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup1.getId(), 1, followup2.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(followup1.getId(), 2));
        AnswerChoice followup2Correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup2.getId(), 1, null));
        AnswerChoice followup2Wrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(followup2.getId(), 2));

        return new QuizData(root, followup1, followup2, rootCorrect, followup1Correct, followup2Correct, followup2Wrong);
    }

    private record QuizData(
            Question root,
            Question followup1,
            Question followup2,
            AnswerChoice rootCorrect,
            AnswerChoice followup1Correct,
            AnswerChoice followup2Correct,
            AnswerChoice followup2Wrong
    ) {

        CreateSolvedSessionCommand toCommand() {
            return new CreateSolvedSessionCommand(
                    new SolvedQuestionCommand(root.getId(), rootCorrect.getId(), followup1.getId()),
                    List.of(
                            new SolvedQuestionCommand(followup1.getId(), followup1Correct.getId(), followup2.getId()),
                            new SolvedQuestionCommand(followup2.getId(), followup2Wrong.getId(), null)
                    )
            );
        }

        CreateSolvedSessionCommand toAllCorrectCommand() {
            return new CreateSolvedSessionCommand(
                    new SolvedQuestionCommand(root.getId(), rootCorrect.getId(), followup1.getId()),
                    List.of(
                            new SolvedQuestionCommand(followup1.getId(), followup1Correct.getId(), followup2.getId()),
                            new SolvedQuestionCommand(followup2.getId(), followup2Correct.getId(), null)
                    )
            );
        }
    }
}
