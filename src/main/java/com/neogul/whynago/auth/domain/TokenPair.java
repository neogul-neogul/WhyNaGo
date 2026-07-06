package com.neogul.whynago.auth.domain;

public record TokenPair(String accessToken, String refreshToken) {
}