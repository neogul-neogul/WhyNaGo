package com.neogul.whynago.auth.fixture;

import com.neogul.whynago.auth.service.dto.SignUpCommand;

public class SignUpCommandFixture {

    public static SignUpCommandBuilder signUpCommand() {
        return new SignUpCommandBuilder();
    }

    public static class SignUpCommandBuilder {

        private String email = "test@example.com";
        private String password = "password123";
        private String nickname = "tester";

        public SignUpCommandBuilder email(String email) {
            this.email = email;
            return this;
        }

        public SignUpCommandBuilder password(String password) {
            this.password = password;
            return this;
        }

        public SignUpCommandBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public SignUpCommand build() {
            return new SignUpCommand(email, password, nickname);
        }
    }
}