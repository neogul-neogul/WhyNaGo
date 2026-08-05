package com.neogul.whynago.wrongnote.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.AnswerChoiceFixture;
import com.neogul.whynago.fixture.QuestionFixture;
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
import com.neogul.whynago.wrongnote.domain.WrongNote;
import com.neogul.whynago.wrongnote.exception.WrongNoteErrorCode;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteBookmarkResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteDetailResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteSummaryResult;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class WrongNoteServiceTest extends IntegrationTestSupport {

    @Autowired
    private WrongNoteService wrongNoteService;

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

    @Autowired
    private WrongNoteRepository wrongNoteRepository;

    @Test
    @DisplayName("사용자의 오답노트 목록을 조회한다.")
    void findAll() {
        Long sessionId = saveMultipleChoiceSession(10L);
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, sessionId));
        wrongNoteRepository.save(WrongNote.create(20L, saveMultipleChoiceSession(20L)));

        List<WrongNoteSummaryResult> result = wrongNoteService.findAll(10L, null);

        assertThat(result).extracting(WrongNoteSummaryResult::id).containsExactly(note.getId());
        assertThat(result.get(0).type()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(result.get(0).category()).isEqualTo(Category.NETWORK);
        assertThat(result.get(0).difficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(result.get(0).isBookmarked()).isFalse();
    }

    @Test
    @DisplayName("오답노트 목록은 재풀이 진입에 사용할 본질문 ID를 함께 반환한다.")
    void findAll_questionIdIsRootQuestion() {
        Long sessionId = saveMultipleChoiceSession(10L);
        wrongNoteRepository.save(WrongNote.create(10L, sessionId));
        Long rootQuestionId = solvedMultipleChoiceRepository.findBySolvedSessionIdOrderBySequence(sessionId)
                .get(0)
                .getQuestionId();

        List<WrongNoteSummaryResult> result = wrongNoteService.findAll(10L, null);

        assertThat(result.get(0).questionId()).isEqualTo(rootQuestionId);
    }

    @Test
    @DisplayName("북마크한 오답노트만 필터링해 조회한다.")
    void findAll_bookmarkedOnly() {
        WrongNote bookmarked = wrongNoteRepository.save(WrongNote.create(10L, saveMultipleChoiceSession(10L)));
        bookmarked.updateBookmark(true);
        wrongNoteRepository.save(WrongNote.create(10L, saveMultipleChoiceSession(10L)));

        List<WrongNoteSummaryResult> result = wrongNoteService.findAll(10L, true);

        assertThat(result).extracting(WrongNoteSummaryResult::id).containsExactly(bookmarked.getId());
    }

    @Test
    @DisplayName("객관식 오답노트 상세를 조회하면 문항별 보기·정답·해설을 반환한다.")
    void findDetail_multipleChoice() {
        Long sessionId = saveMultipleChoiceSession(10L);
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, sessionId));

        WrongNoteDetailResult result = wrongNoteService.findDetail(10L, note.getId());

        assertThat(result.type()).isEqualTo(QuestionType.MULTIPLE_CHOICE);
        assertThat(result.category()).isEqualTo(Category.NETWORK);
        assertThat(result.difficulty()).isEqualTo(Difficulty.MEDIUM);
        assertThat(result.essayItems()).isNull();
        assertThat(result.multipleChoiceItems()).hasSize(2);
        assertThat(result.multipleChoiceItems().get(0).sequence()).isOne();
        assertThat(result.multipleChoiceItems().get(0).choices()).hasSize(2);
        assertThat(result.multipleChoiceItems().get(1).isCorrect()).isFalse();
        assertThat(result.multipleChoiceItems().get(1).choiceExplanation()).isNotBlank();
    }

    @Test
    @DisplayName("서술형 오답노트 상세를 조회하면 문항별 답변·피드백·모범답안을 반환한다.")
    void findDetail_essay() {
        Question essayRoot = questionRepository.save(QuestionFixture.essayRoot());
        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(10L, QuestionType.ESSAY, 3, 2, LocalDateTime.now().minusMinutes(5), LocalDateTime.now()));
        essaySolvedRepository.save(EssaySolved.create(
                session.getId(), 10L, ItemType.MAIN, 1, essayRoot.getId(),
                "격리 수준을 설명하라.", "답변1", "피드백1", "모범답안1", true, LocalDateTime.now()));
        essaySolvedRepository.save(EssaySolved.create(
                session.getId(), 10L, ItemType.FOLLOWUP, 2, null,
                "팬텀 리드는?", "답변2", "피드백2", "모범답안2", false, LocalDateTime.now()));
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, session.getId()));

        WrongNoteDetailResult result = wrongNoteService.findDetail(10L, note.getId());

        assertThat(result.type()).isEqualTo(QuestionType.ESSAY);
        assertThat(result.category()).isEqualTo(Category.DB);
        assertThat(result.multipleChoiceItems()).isNull();
        assertThat(result.essayItems()).hasSize(2);
        assertThat(result.essayItems().get(1).isCorrect()).isFalse();
        assertThat(result.essayItems().get(1).modelAnswer()).isEqualTo("모범답안2");
    }

    @Test
    @DisplayName("존재하지 않거나 소유자가 다른 오답노트를 조회하면 예외가 발생한다.")
    void findDetail_notFound() {
        Long sessionId = saveMultipleChoiceSession(10L);
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, sessionId));

        assertThatThrownBy(() -> wrongNoteService.findDetail(20L, note.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(WrongNoteErrorCode.WRONG_NOTE_NOT_FOUND));
    }

    @Test
    @DisplayName("오답노트 북마크 상태를 변경한다.")
    void updateBookmark() {
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, saveMultipleChoiceSession(10L)));

        WrongNoteBookmarkResult result = wrongNoteService.updateBookmark(10L, note.getId(), true);

        assertThat(result.isBookmarked()).isTrue();
        assertThat(wrongNoteRepository.findById(note.getId())).get()
                .extracting(WrongNote::isBookmarked).isEqualTo(true);
    }

    @Test
    @DisplayName("오답노트를 삭제한다.")
    void delete() {
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, saveMultipleChoiceSession(10L)));

        wrongNoteService.delete(10L, note.getId());

        assertThat(wrongNoteRepository.findById(note.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않거나 소유자가 다른 오답노트를 삭제하면 예외가 발생한다.")
    void delete_notFound() {
        WrongNote note = wrongNoteRepository.save(WrongNote.create(10L, saveMultipleChoiceSession(10L)));

        assertThatThrownBy(() -> wrongNoteService.delete(20L, note.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(WrongNoteErrorCode.WRONG_NOTE_NOT_FOUND));
    }

    private Long saveMultipleChoiceSession(Long userId) {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        AnswerChoice rootCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(root.getId(), 1, followup.getId()));
        answerChoiceRepository.save(AnswerChoiceFixture.wrong(root.getId(), 2));
        AnswerChoice followupCorrect = answerChoiceRepository.save(AnswerChoiceFixture.correct(followup.getId(), 1, null));
        AnswerChoice followupWrong = answerChoiceRepository.save(AnswerChoiceFixture.wrong(followup.getId(), 2));

        SolvedSession session = solvedSessionRepository.save(
                SolvedSession.completed(userId, QuestionType.MULTIPLE_CHOICE, 2, 1, LocalDateTime.now().minusMinutes(5), LocalDateTime.now()));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                session.getId(), userId, root.getId(), ItemType.MAIN, 1,
                rootCorrect.getId(), rootCorrect.getId(), true, LocalDateTime.now()));
        solvedMultipleChoiceRepository.save(SolvedMultipleChoice.create(
                session.getId(), userId, followup.getId(), ItemType.FOLLOWUP, 2,
                followupWrong.getId(), followupCorrect.getId(), false, LocalDateTime.now()));

        return session.getId();
    }
}
