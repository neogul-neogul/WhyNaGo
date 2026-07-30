package com.neogul.whynago.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.ItemType;
import com.neogul.whynago.solvedsession.domain.SessionStatus;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.infra.EssaySolvedRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.solvedsession.service.EssaySolvedSessionService;
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

class EssaySolvingIntegrationTest extends IntegrationTestSupport {

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
    @DisplayName("서술형 본질문과 꼬리질문 2개를 이어 푼 세션을 저장하면 DB에 완료 세션과 문항, 오답노트가 저장된다.")
    void createEssaySolvingSession() {
        Question essayRoot = questionRepository.save(QuestionFixture.essayRoot());
        CreateEssaySolvedSessionCommand command = new CreateEssaySolvedSessionCommand(
                new EssaySolvedQuestionCommand(essayRoot.getId(), "본질문", "답변1", "피드백1", "모범답안1", true),
                List.of(
                        new EssaySolvedQuestionCommand(null, "꼬리질문1", "답변2", "피드백2", "모범답안2", false),
                        new EssaySolvedQuestionCommand(null, "꼬리질문2", "답변3", "피드백3", "모범답안3", true)
                ),
                LocalDateTime.now().minusMinutes(5)
        );

        CreateEssaySolvedSessionResult result = essaySolvedSessionService.create(99L, command);

        SolvedSession session = solvedSessionRepository.findById(result.sessionId()).orElseThrow();
        assertThat(session.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        assertThat(session.getTotalCount()).isEqualTo(3);
        assertThat(session.getCorrectCount()).isEqualTo(2);
        assertThat(wrongNoteRepository.existsByUserIdAndSolvedSessionId(99L, result.sessionId())).isTrue();

        List<EssaySolved> items = essaySolvedRepository.findBySolvedSessionIdOrderBySequence(result.sessionId());
        assertThat(items).hasSize(3);
        assertThat(items).extracting(EssaySolved::getType)
                .containsExactly(ItemType.MAIN, ItemType.FOLLOWUP, ItemType.FOLLOWUP);
        assertThat(items).extracting(EssaySolved::getSequence).containsExactly(1, 2, 3);
        assertThat(items.get(0).getQuestionId()).isEqualTo(essayRoot.getId());
        assertThat(items.get(1).getQuestionId()).isNull();
        assertThat(items.get(2).getModelAnswer()).isEqualTo("모범답안3");
    }
}
