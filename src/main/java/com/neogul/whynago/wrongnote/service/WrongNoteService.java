package com.neogul.whynago.wrongnote.service;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.solvedsession.domain.EssaySolved;
import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.domain.SolvedSession;
import com.neogul.whynago.solvedsession.implement.EssaySolvedReader;
import com.neogul.whynago.solvedsession.implement.SolvedMultipleChoiceReader;
import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import com.neogul.whynago.wrongnote.domain.WrongNote;
import com.neogul.whynago.wrongnote.implement.WrongNoteReader;
import com.neogul.whynago.wrongnote.implement.WrongNoteRemover;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteBookmarkResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteDetailResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteEssayItemResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteMultipleChoiceItemResult;
import com.neogul.whynago.wrongnote.service.dto.WrongNoteSummaryResult;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WrongNoteService {

    private final WrongNoteReader wrongNoteReader;
    private final WrongNoteRemover wrongNoteRemover;
    private final SolvedSessionReader solvedSessionReader;
    private final SolvedMultipleChoiceReader solvedMultipleChoiceReader;
    private final EssaySolvedReader essaySolvedReader;
    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;

    @Transactional(readOnly = true)
    public List<WrongNoteSummaryResult> findAll(Long userId, Boolean bookmarked) {
        return wrongNoteReader.readAll(userId, bookmarked).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public WrongNoteDetailResult findDetail(Long userId, Long wrongNoteId) {
        WrongNote wrongNote = wrongNoteReader.read(userId, wrongNoteId);
        SolvedSession solvedSession = solvedSessionReader.read(wrongNote.getSolvedSessionId());

        if (solvedSession.getType() == QuestionType.ESSAY) {
            return toEssayDetail(wrongNote, solvedSession);
        }
        return toMultipleChoiceDetail(wrongNote, solvedSession);
    }

    @Transactional
    public WrongNoteBookmarkResult updateBookmark(Long userId, Long wrongNoteId, boolean bookmarked) {
        WrongNote wrongNote = wrongNoteReader.read(userId, wrongNoteId);
        wrongNote.updateBookmark(bookmarked);
        return WrongNoteBookmarkResult.from(wrongNote);
    }

    @Transactional
    public void delete(Long userId, Long wrongNoteId) {
        WrongNote wrongNote = wrongNoteReader.read(userId, wrongNoteId);
        wrongNoteRemover.remove(wrongNote);
    }

    private WrongNoteSummaryResult toSummary(WrongNote wrongNote) {
        SolvedSession solvedSession = solvedSessionReader.read(wrongNote.getSolvedSessionId());
        Question rootQuestion = readRootQuestion(wrongNote.getSolvedSessionId(), solvedSession.getType());
        return WrongNoteSummaryResult.from(wrongNote, solvedSession, rootQuestion);
    }

    private Question readRootQuestion(Long solvedSessionId, QuestionType type) {
        if (type == QuestionType.ESSAY) {
            Long rootQuestionId = essaySolvedReader.readOrdered(solvedSessionId).get(0).getQuestionId();
            return questionReader.read(rootQuestionId);
        }
        Long rootQuestionId = solvedMultipleChoiceReader.readOrdered(solvedSessionId).get(0).getQuestionId();
        return questionReader.read(rootQuestionId);
    }

    private WrongNoteDetailResult toMultipleChoiceDetail(WrongNote wrongNote, SolvedSession solvedSession) {
        List<SolvedMultipleChoice> items = solvedMultipleChoiceReader.readOrdered(wrongNote.getSolvedSessionId());
        Question rootQuestion = questionReader.read(items.get(0).getQuestionId());
        List<WrongNoteMultipleChoiceItemResult> itemResults = items.stream()
                .map(this::toMultipleChoiceItem)
                .toList();
        return WrongNoteDetailResult.ofMultipleChoice(wrongNote, solvedSession, rootQuestion, itemResults);
    }

    private WrongNoteMultipleChoiceItemResult toMultipleChoiceItem(SolvedMultipleChoice item) {
        Question question = questionReader.read(item.getQuestionId());
        List<AnswerChoice> choices = answerChoiceReader.readChoices(item.getQuestionId());
        return WrongNoteMultipleChoiceItemResult.from(item, question, choices);
    }

    private WrongNoteDetailResult toEssayDetail(WrongNote wrongNote, SolvedSession solvedSession) {
        List<EssaySolved> items = essaySolvedReader.readOrdered(wrongNote.getSolvedSessionId());
        Question rootQuestion = questionReader.read(items.get(0).getQuestionId());
        List<WrongNoteEssayItemResult> itemResults = items.stream()
                .map(WrongNoteEssayItemResult::from)
                .toList();
        return WrongNoteDetailResult.ofEssay(wrongNote, solvedSession, rootQuestion, itemResults);
    }
}