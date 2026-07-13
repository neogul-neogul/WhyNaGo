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
import com.neogul.whynago.solvedsession.domain.SessionSource;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionCommand;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionResult;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmittedAnswer;
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
    @DisplayName("전체 풀이 결과를 한 번에 제출하면 세션과 문항 결과, 오답노트를 저장한다.")
    void submit() {
        QuizData quizData = saveQuizData();

        SubmitSessionResult result = solvedSessionService.submit(
                10L,
                new SubmitSessionCommand(
                        quizData.root().getId(),
                        SessionSource.PROBLEM_SOLVING,
                        true,
                        List.of(
                                new SubmittedAnswer(quizData.root().getId(), quizData.rootCorrect().getId()),
                                new SubmittedAnswer(quizData.followup().getId(), quizData.followupWrong().getId())
                        )
                )
        );

        assertThat(result.totalCount()).isEqualTo(2);
        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.wrongCount()).isEqualTo(1);
        assertThat(result.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(result.correctRate()).isEqualTo(50.0);
        assertThat(solvedSessionRepository.findById(result.sessionId())).isPresent();
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(10L, result.sessionId())).isTrue();

        List<SolvedMultipleChoice> items = solvedMultipleChoiceRepository.findBySolvedSessionIdOrderBySequence(result.sessionId());
        assertThat(items).hasSize(2);
        assertThat(items.get(0).getType()).isEqualTo(ItemType.MAIN);
        assertThat(items.get(0).getSequence()).isZero();
        assertThat(items.get(0).isCorrect()).isTrue();
        assertThat(items.get(1).getType()).isEqualTo(ItemType.FOLLOWUP);
        assertThat(items.get(1).getSequence()).isOne();
        assertThat(items.get(1).isCorrect()).isFalse();
        assertThat(items.get(1).getAnswerChoiceId()).isEqualTo(quizData.followupCorrect().getId());
    }

    @Test
    @DisplayName("전부 정답이면 오답노트를 만들지 않는다.")
    void submitAllCorrect() {
        QuizData quizData = saveQuizData();

        SubmitSessionResult result = solvedSessionService.submit(
                10L,
                new SubmitSessionCommand(
                        quizData.root().getId(),
                        SessionSource.PROBLEM_SOLVING,
                        false,
                        List.of(
                                new SubmittedAnswer(quizData.root().getId(), quizData.rootCorrect().getId()),
                                new SubmittedAnswer(quizData.followup().getId(), quizData.followupCorrect().getId())
                        )
                )
        );

        assertThat(result.status()).isEqualTo(SessionStatus.ABANDONED);
        assertThat(result.correctCount()).isEqualTo(2);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(10L, result.sessionId())).isFalse();
    }

    @Test
    @DisplayName("루트 문제가 객관식 루트가 아니면 예외가 발생한다.")
    void submitWithInvalidRootQuestion() {
        Question essayRoot = questionRepository.save(QuestionFixture.essayRoot());

        assertThatThrownBy(() -> solvedSessionService.submit(
                10L,
                new SubmitSessionCommand(
                        essayRoot.getId(),
                        SessionSource.PROBLEM_SOLVING,
                        true,
                        List.of(new SubmittedAnswer(essayRoot.getId(), 1L))
                )
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_ROOT));
    }

    @Test
    @DisplayName("선택지가 제출 문항에 속하지 않으면 예외가 발생한다.")
    void submitWithChoiceNotInQuestion() {
        QuizData quizData = saveQuizData();

        assertThatThrownBy(() -> solvedSessionService.submit(
                10L,
                new SubmitSessionCommand(
                        quizData.root().getId(),
                        SessionSource.PROBLEM_SOLVING,
                        true,
                        List.of(new SubmittedAnswer(quizData.root().getId(), quizData.followupCorrect().getId()))
                )
        ))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.CHOICE_NOT_IN_QUESTION));
    }

    private QuizData saveQuizData() {
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice rootCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice followupCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup.getId(), 1, null));
        AnswerChoice followupWrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(followup.getId(), 2));
        return new QuizData(root, followup, rootCorrect, followupCorrect, followupWrong);
    }

    private record QuizData(
            Question root,
            Question followup,
            AnswerChoice rootCorrect,
            AnswerChoice followupCorrect,
            AnswerChoice followupWrong
    ) {
    }
}
