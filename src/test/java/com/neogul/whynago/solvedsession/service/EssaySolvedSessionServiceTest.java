package com.neogul.whynago.solvedsession.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionCommand;
import com.neogul.whynago.solvedsession.service.dto.CreateEssaySolvedSessionResult;
import com.neogul.whynago.solvedsession.service.dto.EssaySolvedQuestionCommand;
import com.neogul.whynago.support.IntegrationTestSupport;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EssaySolvedSessionServiceTest extends IntegrationTestSupport {

    @Autowired
    private EssaySolvedSessionService essaySolvedSessionService;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private SolvedSessionRepository solvedSessionRepository;

    @Autowired
    private EssaySolvedRepository essaySolvedRepository;

    @Autowired
    private WrongNoteRepository wrongNoteRepository;

    @Test
    @DisplayName("본질문과 꼬리질문 문답을 하나의 서술형 세션으로 저장하고 오답이 있으면 오답노트를 만든다.")
    void create() {
        Question essayRoot = questionRepository.save(QuestionFixture.essayRoot());
        CreateEssaySolvedSessionCommand command = command(essayRoot.getId(), true, false, true);

        CreateEssaySolvedSessionResult result = essaySolvedSessionService.create(10L, command);

        SolvedSession session = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getTotalCount()).isEqualTo(3);
        assertThat(session.getCorrectCount()).isEqualTo(2);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(10L, result.sessionId())).isTrue();

        List<EssaySolved> items = essaySolvedRepository.findBySolvedSessionIdOrderBySequence(result.sessionId());
        assertThat(items).hasSize(3);
        assertThat(items.get(0).getType()).isEqualTo(ItemType.MAIN);
        assertThat(items.get(0).getQuestionId()).isEqualTo(essayRoot.getId());
        assertThat(items.get(1).getType()).isEqualTo(ItemType.FOLLOWUP);
        assertThat(items.get(1).getQuestionId()).isNull();
        assertThat(items.get(2).getType()).isEqualTo(ItemType.FOLLOWUP);
        assertThat(items.get(2).isCorrect()).isTrue();
    }

    @Test
    @DisplayName("전부 정답이면 오답노트를 만들지 않는다.")
    void createAllCorrect() {
        Question essayRoot = questionRepository.save(QuestionFixture.essayRoot());
        CreateEssaySolvedSessionCommand command = command(essayRoot.getId(), true, true, true);

        CreateEssaySolvedSessionResult result = essaySolvedSessionService.create(10L, command);

        SolvedSession session = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(session.getCorrectCount()).isEqualTo(3);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(10L, result.sessionId())).isFalse();
    }

    @Test
    @DisplayName("본질문이 서술형이 아니면 예외가 발생한다.")
    void createWithNonEssayRoot() {
        Question multipleChoice = questionRepository.save(QuestionFixture.rootMultipleChoice());
        CreateEssaySolvedSessionCommand command = command(multipleChoice.getId(), true, true, true);

        assertThatThrownBy(() -> essaySolvedSessionService.create(10L, command))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_ESSAY));
    }

    private CreateEssaySolvedSessionCommand command(Long rootQuestionId, boolean main, boolean followup1, boolean followup2) {
        return new CreateEssaySolvedSessionCommand(
                new EssaySolvedQuestionCommand(rootQuestionId, "본질문", "답변1", "피드백1", "모범답안1", main),
                List.of(
                        new EssaySolvedQuestionCommand(null, "꼬리질문1", "답변2", "피드백2", "모범답안2", followup1),
                        new EssaySolvedQuestionCommand(null, "꼬리질문2", "답변3", "피드백3", "모범답안3", followup2)
                ),
                LocalDateTime.now().minusMinutes(5)
        );
    }
}
