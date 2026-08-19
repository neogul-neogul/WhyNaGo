package com.neogul.whynago.admin.service.dto;

import java.util.List;

public record AdminMembersResult(
        List<AdminMemberResult> members,
        int page,
        int size,
        long totalElements
) {
}
