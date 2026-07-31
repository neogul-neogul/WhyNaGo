package com.neogul.whynago.wrongnote.implement;

import com.neogul.whynago.wrongnote.domain.WrongNote;
import com.neogul.whynago.wrongnote.infra.WrongNoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WrongNoteRemover {

    private final WrongNoteRepository wrongNoteRepository;

    public void remove(WrongNote wrongNote) {
        wrongNoteRepository.delete(wrongNote);
    }
}