package com.neogul.whynago.admin.presentation;

import com.neogul.whynago.admin.presentation.dto.AdminMemberDetailResponse;
import com.neogul.whynago.admin.presentation.dto.AdminMemberResponse;
import com.neogul.whynago.admin.presentation.dto.AdminMemberSummaryResponse;
import com.neogul.whynago.admin.service.AdminMemberDetailService;
import com.neogul.whynago.admin.service.AdminMemberListService;
import com.neogul.whynago.admin.service.dto.AdminMemberSearchCommand;
import com.neogul.whynago.admin.service.dto.AdminMembersResult;
import com.neogul.whynago.common.presentation.dto.PageResponse;
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
@RequestMapping("/api/admin/members")
public class AdminMemberController {

    private final AdminMemberListService adminMemberListService;
    private final AdminMemberDetailService adminMemberDetailService;

    @GetMapping
    public ResponseEntity<PageResponse<AdminMemberResponse>> findMembers(
            @RequestParam(required = false, name = "q") String keyword,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        AdminMemberSearchCommand command = AdminMemberSearchCommand.of(keyword, page, size);
        AdminMembersResult result = adminMemberListService.readMembers(command);
        List<AdminMemberResponse> responses = result.members().stream()
                .map(AdminMemberResponse::from)
                .toList();

        return ResponseEntity.ok(PageResponse.of(responses, result.page(), result.size(), result.totalElements()));
    }

    // 목록 응답(PageResponse)에 끼울 수 없는 요약 두 값을 따로 내려준다.
    @GetMapping("/summary")
    public ResponseEntity<AdminMemberSummaryResponse> findSummary() {
        return ResponseEntity.ok(AdminMemberSummaryResponse.from(adminMemberListService.readSummary()));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminMemberDetailResponse> findMember(@PathVariable Long userId) {
        return ResponseEntity.ok(AdminMemberDetailResponse.from(adminMemberDetailService.readMember(userId)));
    }
}
