package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.service.QuestionService;
import com.neogul.whynago.question.service.dto.ChoiceGradingResult;
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
    private QuestionService questionService;

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

    @Test
    @DisplayName("보기 선택 결과 조회로 고른 보기의 꼬리질문을 따라가며 푼 뒤 저장하면 완료 세션이 저장된다.")
    void solveFollowingChoiceGradings() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question correctBranch = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question wrongBranch = questionRepository.save(QuestionFixture.followupMultipleChoice());
        Question last = questionRepository.save(QuestionFixture.followupMultipleChoice());
        answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, correctBranch.getId()));
        AnswerChoice rootWrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2, wrongBranch.getId()));
        AnswerChoice wrongBranchCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(wrongBranch.getId(), 1, last.getId()));
        AnswerChoice lastCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(last.getId(), 1, null));

        // 본질문: 오답 보기를 골라도 그 보기에 연결된 꼬리질문으로 분기한다.
        ChoiceGradingResult rootGrading = questionService.getChoiceGrading(root.getId(), rootWrong.getId());
        assertThat(rootGrading.correct()).isFalse();
        assertThat(rootGrading.nextQuestion().id()).isEqualTo(wrongBranch.getId());

        ChoiceGradingResult followupGrading = questionService.getChoiceGrading(wrongBranch.getId(), wrongBranchCorrect.getId());
        assertThat(followupGrading.nextQuestion().id()).isEqualTo(last.getId());

        ChoiceGradingResult lastGrading = questionService.getChoiceGrading(last.getId(), lastCorrect.getId());
        assertThat(lastGrading.nextQuestion()).isNull();

        CreateSolvedSessionCommand command = new CreateSolvedSessionCommand(
                new SolvedQuestionCommand(root.getId(), rootWrong.getId(), wrongBranch.getId()),
                List.of(
                        new SolvedQuestionCommand(wrongBranch.getId(), wrongBranchCorrect.getId(), last.getId()),
                        new SolvedQuestionCommand(last.getId(), lastCorrect.getId(), null)
                )
        );

        CreateSolvedSessionResult result = solvedSessionService.create(99L, command);

        SolvedSession savedSession = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(savedSession.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(savedSession.getTotalCount()).isEqualTo(3);
        assertThat(savedSession.getCorrectCount()).isEqualTo(2);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(99L, result.sessionId())).isTrue();
    }
}
