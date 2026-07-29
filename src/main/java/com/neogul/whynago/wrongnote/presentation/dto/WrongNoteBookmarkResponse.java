package com.neogul.whynago.wrongnote.presentation.dto;

import com.neogul.whynago.wrongnote.service.dto.WrongNoteBookmarkResult;

public record WrongNoteBookmarkResponse(boolean isBookmarked) {

    public static WrongNoteBookmarkResponse from(WrongNoteBookmarkResult result) {
        return new WrongNoteBookmarkResponse(result.isBookmarked());
    }
}
