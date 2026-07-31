package com.neogul.whynago.user.domain;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.user.exception.UserErrorCode;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private static final int NICKNAME_MIN_LENGTH = 4;
    private static final int NICKNAME_MAX_LENGTH = 8;

    // 아직 가입 시 직접 설정하는 UI가 없어 기본값으로 고정한다 — 마이페이지 최소 학습 목표 기본값
    private static final int DEFAULT_DAILY_GOAL = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Email email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Position position;

    @Column(nullable = false)
    private int dailyGoal;

    private User(Email email, String password, String nickname, Position position, int dailyGoal) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.position = position;
        this.dailyGoal = dailyGoal;
    }

    // 직무 BACKEND로 고정
    public static User create(String email, String password, String nickname) {
        validateNickname(nickname);
        return new User(new Email(email), password, nickname, Position.BACKEND, DEFAULT_DAILY_GOAL);
    }

    public void updateDailyGoal(int dailyGoal) {
        this.dailyGoal = dailyGoal;
    }

    private static void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()
                || nickname.length() < NICKNAME_MIN_LENGTH || nickname.length() > NICKNAME_MAX_LENGTH) {
            throw new BusinessException(UserErrorCode.USER_INVALID_NICKNAME);
        }
    }
}
