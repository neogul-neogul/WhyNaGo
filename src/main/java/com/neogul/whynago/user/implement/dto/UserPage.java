package com.neogul.whynago.user.implement.dto;

import com.neogul.whynago.user.domain.User;
import java.util.List;

public record UserPage(
        List<User> users,
        long totalElements
) {
}
