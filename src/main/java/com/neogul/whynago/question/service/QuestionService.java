package com.neogul.whynago.question.service;

import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.AnswerChoiceReader;
import com.neogul.whynago.question.implement.QuestionReader;
import com.neogul.whynago.question.service.dto.ChoiceResult;
import com.neogul.whynago.question.service.dto.EssayQuestionResult;
import com.neogul.whynago.question.service.dto.QuestionResult;
import com.neogul.whynago.question.service.dto.QuestionSearchCommand;
import java.util.List;
import java.util.Map;
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
        Map<Long, List<String>> tagsByQuestionId = questionReader.readTagNames(questions.stream()
                .map(Question::getId)
                .toList());

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

    public EssayQuestionResult findEssayQuestion(Long questionId) {
        Question question = questionReader.readEssayQuestion(questionId);
        List<String> tags = questionReader.readTagNames(List.of(question.getId()))
                .getOrDefault(question.getId(), List.of());

        return EssayQuestionResult.from(question, tags);
    }
}
