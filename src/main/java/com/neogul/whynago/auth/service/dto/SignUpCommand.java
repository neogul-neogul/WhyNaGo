package com.neogul.whynago.auth.service.dto;

public record SignUpCommand(String email, String password, String nickname) {
}