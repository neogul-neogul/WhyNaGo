package com.neogul.whynago.user.service;

import com.neogul.whynago.user.implement.UserReader;
import com.neogul.whynago.user.service.dto.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserReader userReader;

    @Transactional(readOnly = true)
    public UserProfileResult getProfile(Long userId) {
        return UserProfileResult.from(userReader.read(userId));
    }
}