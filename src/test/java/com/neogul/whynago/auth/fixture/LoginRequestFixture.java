package com.neogul.whynago.auth.fixture;

import com.neogul.whynago.auth.presentation.dto.LoginRequest;

public class LoginRequestFixture {

    public static LoginRequestBuilder loginRequest() {
        return new LoginRequestBuilder();
    }

    public static class LoginRequestBuilder {

        private String email = "test@example.com";
        private String password = "password123";

        public LoginRequestBuilder email(String email) {
            this.email = email;
            return this;
        }

        public LoginRequestBuilder password(String password) {
            this.password = password;
            return this;
        }

        public LoginRequest build() {
            return new LoginRequest(email, password);
        }
    }
}