package com.neogul.whynago.wrongnote.implement;

import com.neogul.whynago.wrongnote.domain.WrongNote;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WrongNoteAppender {

    private final WrongNoteRepository wrongNoteRepository;

    public void appendIfWrongAnswer(Long userId, Long solvedSessionId, boolean hasWrongAnswer) {
        if (hasWrongAnswer) {
            appendIfAbsent(userId, solvedSessionId);
        }
    }

    public void appendIfAbsent(Long userId, Long solvedSessionId) {
        if (!wrongNoteRepository.existsByUserIdAndSolvedSessionId(userId, solvedSessionId)) {
            wrongNoteRepository.save(WrongNote.create(userId, solvedSessionId));
        }
    }
}
