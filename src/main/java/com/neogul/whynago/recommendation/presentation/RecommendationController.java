package com.neogul.whynago.recommendation.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.recommendation.presentation.dto.RecommendationResponse;
import com.neogul.whynago.recommendation.presentation.dto.WeakTagsResponse;
import com.neogul.whynago.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/questions")
    public ResponseEntity<RecommendationResponse> recommendQuestions(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(
                RecommendationResponse.from(recommendationService.recommend(authContext.id())));
    }

    @GetMapping("/weak-tags")
    public ResponseEntity<WeakTagsResponse> weakTags(@LoginUser AuthContext authContext) {
        return ResponseEntity.ok(
                WeakTagsResponse.from(recommendationService.weakTags(authContext.id())));
    }
}
