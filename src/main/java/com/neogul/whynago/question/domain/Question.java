package com.neogul.whynago.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private Category category;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private Question(
            String title,
            String content,
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String explanation
    ) {
        this.title = title;
        this.content = content;
        this.type = type;
        this.difficulty = difficulty;
        this.category = category;
        this.explanation = explanation;
    }

    public static Question create(
            String title,
            String content,
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String explanation
    ) {
        return new Question(title, content, type, difficulty, category, explanation);
    }
}
