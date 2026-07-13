package com.neogul.whynago.question.service;

import com.neogul.whynago.question.domain.AnswerChoice;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Difficulty;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.domain.QuestionTag;
import com.neogul.whynago.question.domain.QuestionType;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionReader questionReader;
    private final AnswerChoiceReader answerChoiceReader;

    public List<QuestionResult> findQuestions(QuestionSearchCommand command) {
        List<Question> questions = questionReader.readRootMultipleChoices(
                command.type(),
                command.difficulty(),
                command.category(),
                command.keyword()
        );
        List<Long> questionIds = questions.stream()
                .map(Question::getId)
                .toList();
        Map<Long, List<String>> tagsByQuestionId = questionReader.readTags(questionIds).stream()
                .collect(Collectors.groupingBy(
                        QuestionTag::getQuestionId,
                        Collectors.mapping(QuestionTag::getName, Collectors.toList())
                ));

        return questions.stream()
                .map(question -> QuestionResult.from(
                        question,
                        answerChoiceReader.readChoices(question.getId()).stream()
                                .map(ChoiceResult::from)
                                .toList(),
                        tagsByQuestionId.getOrDefault(question.getId(), List.of())
                ))
                .toList();
    }

    public record QuestionSearchCommand(
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String keyword
    ) {
    }

    public record QuestionResult(
            Long id,
            String title,
            String content,
            QuestionType type,
            Difficulty difficulty,
            Category category,
            String explanation,
            boolean root,
            List<ChoiceResult> choices,
            List<String> tags
    ) {

        static QuestionResult from(Question question, List<ChoiceResult> choices, List<String> tags) {
            return new QuestionResult(
                    question.getId(),
                    question.getTitle(),
                    question.getContent(),
                    question.getType(),
                    question.getDifficulty(),
                    question.getCategory(),
                    question.getExplanation(),
                    question.isRoot(),
                    choices,
                    tags
            );
        }
    }

    public record ChoiceResult(
            Long id,
            String content,
            int sequence,
            String explanation,
            Long relatedQuestionId
    ) {

        static ChoiceResult from(AnswerChoice answerChoice) {
            return new ChoiceResult(
                    answerChoice.getId(),
                    answerChoice.getContent(),
                    answerChoice.getSequence(),
                    answerChoice.getExplanation(),
                    answerChoice.getRelatedQuestionId()
            );
        }
    }
}
