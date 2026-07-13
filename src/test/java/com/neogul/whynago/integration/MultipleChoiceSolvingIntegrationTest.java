package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.SessionSource;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionCommand;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmitSessionResult;
import com.neogul.whynago.solvedsession.service.SolvedSessionService.SubmittedAnswer;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MultipleChoiceSolvingIntegrationTest extends IntegrationTestSupport {

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
    @DisplayName("전체 객관식 풀이 결과를 제출하면 DB에 완료 세션이 저장된다.")
    void submitMultipleChoiceSolving() {
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        AnswerChoice rootCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        AnswerChoice followupCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup.getId(), 1, null));
        AnswerChoice followupWrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(followup.getId(), 2));

        SubmitSessionResult result = solvedSessionService.submit(
                99L,
                new SubmitSessionCommand(
                        root.getId(),
                        SessionSource.PROBLEM_SOLVING,
                        true,
                        List.of(
                                new SubmittedAnswer(root.getId(), rootCorrect.getId()),
                                new SubmittedAnswer(followup.getId(), followupWrong.getId())
                        )
                )
        );

        assertThat(result.status()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(result.correctCount()).isEqualTo(1);
        assertThat(result.items().get(1).correctChoiceId()).isEqualTo(followupCorrect.getId());
        assertThat(solvedSessionRepository.findById(result.sessionId())).isPresent();
        assertThat(solvedMultipleChoiceRepository.findBySolvedSessionIdOrderBySequence(result.sessionId())).hasSize(2);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(99L, result.sessionId())).isTrue();
    }
}
