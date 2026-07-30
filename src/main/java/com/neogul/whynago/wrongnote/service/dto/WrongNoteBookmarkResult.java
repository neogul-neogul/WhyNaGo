package com.neogul.whynago.wrongnote.service.dto;

import com.neogul.whynago.wrongnote.domain.WrongNote;

public record WrongNoteBookmarkResult(
        boolean isBookmarked
) {

    public static WrongNoteBookmarkResult from(WrongNote wrongNote) {
        return new WrongNoteBookmarkResult(wrongNote.isBookmarked());
    }
}