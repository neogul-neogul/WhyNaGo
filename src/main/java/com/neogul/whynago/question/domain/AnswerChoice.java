package com.neogul.whynago.question.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AnswerChoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private int sequence;

    @Column(nullable = false)
    private boolean isCorrect;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    private Long relatedQuestionId;

    private AnswerChoice(
            Long questionId,
            String content,
            int sequence,
            boolean isCorrect,
            String explanation,
            Long relatedQuestionId
    ) {
        this.questionId = questionId;
        this.content = content;
        this.sequence = sequence;
        this.isCorrect = isCorrect;
        this.explanation = explanation;
        this.relatedQuestionId = relatedQuestionId;
    }

    public static AnswerChoice create(
            Long questionId,
            String content,
            int sequence,
            boolean isCorrect,
            String explanation,
            Long relatedQuestionId
    ) {
        return new AnswerChoice(questionId, content, sequence, isCorrect, explanation, relatedQuestionId);
    }

    public boolean correct() {
        return isCorrect;
    }

    public Long nextQuestionId() {
        return relatedQuestionId;
    }
}
