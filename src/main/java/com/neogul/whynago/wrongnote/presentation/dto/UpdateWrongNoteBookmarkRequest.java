package com.neogul.whynago.wrongnote.presentation.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateWrongNoteBookmarkRequest(
        @NotNull Boolean bookmarked
) {
}
