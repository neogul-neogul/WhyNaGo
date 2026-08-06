package com.neogul.whynago.notification.presentation;

import com.neogul.whynago.auth.presentation.AuthContext;
import com.neogul.whynago.auth.presentation.resolver.LoginUser;
import com.neogul.whynago.notification.presentation.dto.NotificationSettingResponse;
import com.neogul.whynago.notification.presentation.dto.UpdateNotificationSettingRequest;
import com.neogul.whynago.notification.service.NotificationSettingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification-settings")
public class NotificationSettingController {

    private final NotificationSettingService notificationSettingService;

    @GetMapping("/me")
    public ResponseEntity<NotificationSettingResponse> getSettings(@LoginUser AuthContext authContext) {
        NotificationSettingResponse response =
                NotificationSettingResponse.from(notificationSettingService.getSettings(authContext.id()));
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<NotificationSettingResponse> updateSettings(
            @LoginUser AuthContext authContext,
            @Valid @RequestBody UpdateNotificationSettingRequest request) {
        NotificationSettingResponse response = NotificationSettingResponse.from(
                notificationSettingService.updateSettings(authContext.id(), request.toCommand()));
        return ResponseEntity.ok(response);
    }
}
