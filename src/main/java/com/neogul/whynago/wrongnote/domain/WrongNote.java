package com.neogul.whynago.wrongnote.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_wrong_note_user_session", columnNames = {"user_id", "solved_session_id"}))
public class WrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long solvedSessionId;

    @Column(nullable = false)
    private boolean isBookmarked;

    private WrongNote(Long userId, Long solvedSessionId, boolean isBookmarked) {
        this.userId = userId;
        this.solvedSessionId = solvedSessionId;
        this.isBookmarked = isBookmarked;
    }

    public static WrongNote create(Long userId, Long solvedSessionId) {
        return new WrongNote(userId, solvedSessionId, false);
    }
}
