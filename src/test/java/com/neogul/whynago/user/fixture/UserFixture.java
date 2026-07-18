package com.neogul.whynago.user.fixture;

import com.neogul.whynago.user.domain.User;

public class UserFixture {

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static class UserBuilder {

        private String email = "test@example.com";
        private String password = "password123";
        private String nickname = "tester";

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public User build() {
            return User.create(email, password, nickname);
        }
    }
}