package com.neogul.whynago.problemset.service;

import com.neogul.whynago.problemset.domain.ProblemSet;
import com.neogul.whynago.problemset.domain.ProblemSetItem;
import com.neogul.whynago.problemset.implement.ProblemSetItemAppender;
import com.neogul.whynago.problemset.implement.ProblemSetItemReader;
import com.neogul.whynago.problemset.implement.ProblemSetItemRemover;
import com.neogul.whynago.problemset.implement.ProblemSetReader;
import com.neogul.whynago.problemset.implement.ProblemSetAppender;
import com.neogul.whynago.problemset.implement.ProblemSetRemover;
import com.neogul.whynago.problemset.service.dto.CreateProblemSetCommand;
import com.neogul.whynago.problemset.service.dto.CreateProblemSetResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetDetailResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetItemResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetMembershipResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetSummaryResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.implement.QuestionReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProblemSetService {

    private static final int PREVIEW_SIZE = 3;

    private final ProblemSetReader problemSetReader;
    private final ProblemSetAppender problemSetAppender;
    private final ProblemSetRemover problemSetRemover;
    private final ProblemSetItemReader problemSetItemReader;
    private final ProblemSetItemAppender problemSetItemAppender;
    private final ProblemSetItemRemover problemSetItemRemover;
    private final QuestionReader questionReader;

    @Transactional
    public CreateProblemSetResult create(CreateProblemSetCommand command) {
        ProblemSet problemSet = problemSetAppender.append(command.userId(), command.name());
        return CreateProblemSetResult.from(problemSet);
    }

    @Transactional(readOnly = true)
    public List<ProblemSetSummaryResult> findAll(Long userId) {
        List<ProblemSet> problemSets = problemSetReader.readAll(userId);
        Map<Long, List<ProblemSetItem>> itemsByProblemSetId = problemSetItemReader.readAllGroupedByProblemSetId(
                problemSets.stream().map(ProblemSet::getId).toList());
        Map<Long, Question> questionsById = readQuestionsById(itemsByProblemSetId.values().stream()
                .flatMap(List::stream)
                .toList());

        return problemSets.stream()
                .map(problemSet -> toSummary(problemSet, itemsByProblemSetId.getOrDefault(problemSet.getId(), List.of()), questionsById))
                .toList();
    }

    @Transactional(readOnly = true)
    public ProblemSetDetailResult findDetail(Long userId, Long problemSetId) {
        ProblemSet problemSet = problemSetReader.read(userId, problemSetId);
        List<ProblemSetItem> items = problemSetItemReader.readAll(problemSetId);
        Map<Long, Question> questionsById = readQuestionsById(items);

        List<ProblemSetItemResult> itemResults = items.stream()
                .map(item -> ProblemSetItemResult.from(questionsById.get(item.getQuestionId())))
                .toList();
        return ProblemSetDetailResult.of(problemSet, itemResults);
    }

    @Transactional(readOnly = true)
    public List<ProblemSetMembershipResult> findMembership(Long userId, Long questionId) {
        List<ProblemSet> problemSets = problemSetReader.readAll(userId);
        Map<Long, List<ProblemSetItem>> itemsByProblemSetId = problemSetItemReader.readAllGroupedByProblemSetId(
                problemSets.stream().map(ProblemSet::getId).toList());

        return problemSets.stream()
                .map(problemSet -> {
                    List<ProblemSetItem> items = itemsByProblemSetId.getOrDefault(problemSet.getId(), List.of());
                    boolean saved = items.stream().anyMatch(item -> item.getQuestionId().equals(questionId));
                    return ProblemSetMembershipResult.of(problemSet, items.size(), saved);
                })
                .toList();
    }

    @Transactional
    public void addItem(Long userId, Long problemSetId, Long questionId) {
        ProblemSet problemSet = problemSetReader.read(userId, problemSetId);
        questionReader.read(questionId);
        boolean added = problemSetItemAppender.appendIfAbsent(problemSetId, questionId);
        if (added) {
            problemSet.touch();
        }
    }

    @Transactional
    public void removeItem(Long userId, Long problemSetId, Long questionId) {
        ProblemSet problemSet = problemSetReader.read(userId, problemSetId);
        boolean removed = problemSetItemRemover.remove(problemSetId, questionId);
        if (removed) {
            problemSet.touch();
        }
    }

    @Transactional
    public void delete(Long userId, Long problemSetId) {
        ProblemSet problemSet = problemSetReader.read(userId, problemSetId);
        problemSetItemRemover.removeAllByProblemSetId(problemSetId);
        problemSetRemover.remove(problemSet);
    }

    private ProblemSetSummaryResult toSummary(
            ProblemSet problemSet,
            List<ProblemSetItem> items,
            Map<Long, Question> questionsById
    ) {
        List<String> previewTitles = items.stream()
                .limit(PREVIEW_SIZE)
                .map(item -> questionsById.get(item.getQuestionId()).getTitle())
                .toList();
        return ProblemSetSummaryResult.of(problemSet, items.size(), previewTitles);
    }

    private Map<Long, Question> readQuestionsById(List<ProblemSetItem> items) {
        List<Long> questionIds = items.stream()
                .map(ProblemSetItem::getQuestionId)
                .distinct()
                .toList();
        return questionReader.readAll(questionIds).stream()
                .collect(Collectors.toMap(Question::getId, question -> question));
    }
}
