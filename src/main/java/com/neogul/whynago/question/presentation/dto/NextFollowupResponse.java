package com.neogul.whynago.question.presentation.dto;

import com.neogul.whynago.question.service.dto.NextFollowupResult;

public record NextFollowupResponse(String question) {

    static NextFollowupResponse from(NextFollowupResult result) {
        return new NextFollowupResponse(result.question());
    }
}
