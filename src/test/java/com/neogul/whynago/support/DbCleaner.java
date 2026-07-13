package com.neogul.whynago.support;

import com.neogul.whynago.question.infra.AnswerChoiceRepository;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.question.infra.QuestionTagRepository;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import com.neogul.whynago.solvedsession.infra.SolvedSessionRepository;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DbCleaner {

    private final WrongNoteRepository wrongNoteRepository;
    private final SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;
    private final SolvedSessionRepository solvedSessionRepository;
    private final AnswerChoiceRepository answerChoiceRepository;
    private final QuestionTagRepository questionTagRepository;
    private final QuestionRepository questionRepository;

    @Transactional
    public void clear() {
        wrongNoteRepository.deleteAllInBatch();
        solvedMultipleChoiceRepository.deleteAllInBatch();
        solvedSessionRepository.deleteAllInBatch();
        answerChoiceRepository.deleteAllInBatch();
        questionTagRepository.deleteAllInBatch();
        questionRepository.deleteAllInBatch();
    }
}
