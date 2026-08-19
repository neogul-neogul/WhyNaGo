package com.neogul.whynago.admin.presentation;

import com.neogul.whynago.admin.presentation.dto.AdminQuestionDetailResponse;
import com.neogul.whynago.admin.presentation.dto.AdminQuestionResponse;
import com.neogul.whynago.admin.service.AdminQuestionDetailService;
import com.neogul.whynago.admin.service.AdminQuestionListService;
import com.neogul.whynago.admin.service.dto.AdminQuestionsResult;
import com.neogul.whynago.common.presentation.dto.PageResponse;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// 조회는 요청자와 무관하고 권한은 AdminInterceptor가 이미 확인했으므로 로그인 사용자를 받지 않는다.
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/questions")
public class AdminQuestionController {

    private final AdminQuestionListService adminQuestionListService;
    private final AdminQuestionDetailService adminQuestionDetailService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminQuestionResponse>> findQuestions(
            @RequestParam(required = false) QuestionType type,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(required = false) Category category,
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        QuestionSearchCommand command = QuestionSearchCommand.of(type, difficulty, category, keyword, page, size);
        AdminQuestionsResult result = adminQuestionListService.readQuestions(command);
        List<AdminQuestionResponse> responses = result.questions().stream()
                .map(AdminQuestionResponse::from)
                .toList();

        return ResponseEntity.ok(PageResponse.of(responses, result.page(), result.size(), result.totalElements()));
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<AdminQuestionDetailResponse> findQuestion(@PathVariable Long questionId) {
        return ResponseEntity.ok(AdminQuestionDetailResponse.from(adminQuestionDetailService.readQuestion(questionId)));
    }
}
