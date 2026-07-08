package com.neogul.whynago.auth.fixture;

import com.neogul.whynago.auth.presentation.dto.SignUpRequest;

public class SignUpRequestFixture {

    public static SignUpRequestBuilder signUpRequest() {
        return new SignUpRequestBuilder();
    }

    public static class SignUpRequestBuilder {

        private String email = "test@example.com";
        private String password = "password123";
        private String nickname = "tester";

        public SignUpRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public SignUpRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SignUpRequestBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public SignUpRequest build() {
            return new SignUpRequest(email, password, nickname);
        }
    }
}