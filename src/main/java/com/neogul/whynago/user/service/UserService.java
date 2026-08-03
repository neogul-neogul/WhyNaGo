package com.neogul.whynago.user.service;

import com.neogul.whynago.user.domain.User;
import com.neogul.whynago.user.implement.UserReader;
import com.neogul.whynago.user.implement.UserValidator;
import com.neogul.whynago.user.service.dto.UpdateProfileCommand;
import com.neogul.whynago.user.service.dto.UserProfileResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserReader userReader;
    private final UserValidator userValidator;

    @Transactional(readOnly = true)
    public UserProfileResult getProfile(Long userId) {
        return UserProfileResult.from(userReader.read(userId));
    }

    @Transactional
    public UserProfileResult updateProfile(Long userId, UpdateProfileCommand command) {
        User user = userReader.read(userId);
        userValidator.validateUniqueForUpdate(userId, command.email(), command.nickname());
        user.updateProfile(command.email(), command.nickname(), command.position(), command.dailyGoal());
        return UserProfileResult.from(user);
    }
}