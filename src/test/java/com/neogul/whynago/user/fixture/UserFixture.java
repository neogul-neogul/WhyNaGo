package com.neogul.whynago.user.fixture;

import com.neogul.whynago.user.domain.AuthProvider;
import com.neogul.whynago.user.domain.Role;
import com.neogul.whynago.user.domain.User;
import java.time.LocalDateTime;
import org.springframework.test.util.ReflectionTestUtils;

public class UserFixture {

    public static UserBuilder user() {
        return new UserBuilder();
    }

    public static SocialUserBuilder socialUser() {
        return new SocialUserBuilder();
    }

    // 프로덕션에는 승격 경로가 없으므로(운영 DB에서 직접 변경) 테스트에서만 주입한다
    private static User withRole(User user, Role role) {
        ReflectionTestUtils.setField(user, "role", role);
        return user;
    }

    // 가입 시각은 프로덕션에서 가입 시점에만 정해지므로 테스트에서만 주입한다
    private static User withCreatedAt(User user, LocalDateTime createdAt) {
        ReflectionTestUtils.setField(user, "createdAt", createdAt);
        return user;
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
        private Role role = Role.USER;
        private LocalDateTime createdAt = LocalDateTime.of(2026, 8, 19, 10, 0);
        private boolean createdAtTracked = true;

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

        public UserBuilder role(Role role) {
            this.role = role;
            return this;
        }

        public UserBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        /** 가입 시각 추적 이전에 가입해 createdAt이 null인 회원 */
        public UserBuilder createdAtNotTracked() {
            this.createdAtTracked = false;
            return this;
        }

        public User build() {
            User user = withRole(User.create(email, password, nickname), role);
            return withCreatedAt(user, createdAtTracked ? createdAt : null);
        }
    }
}