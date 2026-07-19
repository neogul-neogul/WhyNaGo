package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.solvedsession.service.SolvedSessionService;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionCommand;
import com.neogul.whynago.solvedsession.service.dto.CreateSolvedSessionResult;
import com.neogul.whynago.solvedsession.service.dto.SolvedQuestionCommand;
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
    @DisplayName("본질문 1개와 꼬리질문 2개를 이어 푼 세션을 저장하면 DB에 완료 세션과 문항, 오답노트가 저장된다.")
    void createMultipleChoiceSolvingSession() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup1 = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question followup2 = questionRepository.save(QuestionFixture.followupMultipleChoice());
        AnswerChoice rootCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup1.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice followup1Correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup1.getId(), 1, followup2.getId()));
        AnswerChoice followup2Correct = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup2.getId(), 1, null));
        AnswerChoice followup2Wrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(followup2.getId(), 2));

        CreateSolvedSessionCommand command = new CreateSolvedSessionCommand(
                new SolvedQuestionCommand(root.getId(), rootCorrect.getId(), followup1.getId()),
                List.of(
                        new SolvedQuestionCommand(followup1.getId(), followup1Correct.getId(), followup2.getId()),
                        new SolvedQuestionCommand(followup2.getId(), followup2Wrong.getId(), null)
                )
        );

        CreateSolvedSessionResult result = solvedSessionService.create(99L, command);

        SolvedSession savedSession = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(savedSession.getTotalCount()).isEqualTo(3);
        assertThat(savedSession.getCorrectCount()).isEqualTo(2);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(99L, result.sessionId())).isTrue();

        List<SolvedMultipleChoice> items = solvedMultipleChoiceRepository.findBySolvedSessionIdOrderBySequence(result.sessionId());
        assertThat(items).hasSize(3);
        assertThat(items.get(2).getUserChoiceId()).isEqualTo(followup2Wrong.getId());
        assertThat(items.get(2).getAnswerChoiceId()).isEqualTo(followup2Correct.getId());
        assertThat(items.get(2).isCorrect()).isFalse();
    }
}
