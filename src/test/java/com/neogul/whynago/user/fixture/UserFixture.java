package com.neogul.whynago.user.fixture;

import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.User;

public class UserFixture {

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static SocialUserBuilder socialUser() {
        return new SocialUserBuilder();
    }

    public static class SocialUserBuilder {

        private String email = "social@example.com";
        private String nickname = "u123456";
        private AuthProvider provider = AuthProvider.GOOGLE;
        private String providerId = "google-sub-1";

        public SocialUserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public SocialUserBuilder nickname(String nickname) {
            this.nickname = nickname;
            return this;
        }

        public SocialUserBuilder provider(AuthProvider provider) {
            this.provider = provider;
            return this;
        }

        public SocialUserBuilder providerId(String providerId) {
            this.providerId = providerId;
            return this;
        }

        public User build() {
            return User.createSocial(email, nickname, provider, providerId);
        }
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