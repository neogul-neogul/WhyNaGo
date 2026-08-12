package com.neogul.whynago.problemset.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.neogul.whynago.common.exception.BusinessException;
import com.neogul.whynago.fixture.QuestionFixture;
import com.neogul.whynago.problemset.domain.ProblemSet;
import com.neogul.whynago.problemset.exception.ProblemSetErrorCode;
import com.neogul.whynago.problemset.infra.ProblemSetItemRepository;
import com.neogul.whynago.problemset.infra.ProblemSetRepository;
import com.neogul.whynago.problemset.service.dto.CreateProblemSetCommand;
import com.neogul.whynago.problemset.service.dto.CreateProblemSetResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetDetailResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetMembershipResult;
import com.neogul.whynago.problemset.service.dto.ProblemSetSummaryResult;
import com.neogul.whynago.question.domain.Question;
import com.neogul.whynago.question.exception.QuestionErrorCode;
import com.neogul.whynago.question.infra.QuestionRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class ProblemSetServiceTest extends IntegrationTestSupport {

    @Autowired
    private ProblemSetService problemSetService;

    @Autowired
    private ProblemSetRepository problemSetRepository;

    @Autowired
    private ProblemSetItemRepository problemSetItemRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Test
    @DisplayName("문제집을 생성하면 빈 문제집으로 만들어진다.")
    void create() {
        CreateProblemSetResult result = problemSetService.create(new CreateProblemSetCommand(10L, "면접 D-7 벼락치기"));

        assertThat(result.name()).isEqualTo("면접 D-7 벼락치기");
        assertThat(problemSetItemRepository.findByProblemSetIdOrderByIdAsc(result.id())).isEmpty();
    }

    @Test
    @DisplayName("사용자의 문제집 목록을 담긴 문제 수·미리보기와 함께 조회한다.")
    void findAll() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        Question followup = questionRepository.save(QuestionFixture.followupMultipleChoice());
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));
        problemSetService.addItem(10L, problemSet.getId(), root.getId());
        problemSetService.addItem(10L, problemSet.getId(), followup.getId());
        problemSetRepository.save(ProblemSet.create(20L, "다른 사용자 문제집"));

        List<ProblemSetSummaryResult> result = problemSetService.findAll(10L);

        assertThat(result).extracting(ProblemSetSummaryResult::id).containsExactly(problemSet.getId());
        assertThat(result.get(0).itemCount()).isEqualTo(2);
        assertThat(result.get(0).previewTitles()).containsExactly(root.getTitle(), followup.getTitle());
    }

    @Test
    @DisplayName("문제집 상세를 조회하면 담긴 문제 정보를 함께 반환한다.")
    void findDetail() {
        Question root = questionRepository.save(QuestionFixture.rootMultipleChoice());
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));
        problemSetService.addItem(10L, problemSet.getId(), root.getId());

        ProblemSetDetailResult result = problemSetService.findDetail(10L, problemSet.getId());

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).questionId()).isEqualTo(root.getId());
        assertThat(result.items().get(0).title()).isEqualTo(root.getTitle());
        assertThat(result.items().get(0).category()).isEqualTo(root.getCategory());
    }

    @Test
    @DisplayName("존재하지 않거나 소유자가 다른 문제집을 조회하면 예외가 발생한다.")
    void findDetail_notFound() {
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));

        assertThatThrownBy(() -> problemSetService.findDetail(20L, problemSet.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(ProblemSetErrorCode.PROBLEM_SET_NOT_FOUND));
    }

    @Test
    @DisplayName("특정 문제 기준으로 내 문제집의 저장 여부를 조회한다.")
    void findMembership() {
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        ProblemSet saved = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));
        problemSetService.addItem(10L, saved.getId(), question.getId());
        ProblemSet notSaved = problemSetRepository.save(ProblemSet.create(10L, "네트워크 집중 보완"));

        List<ProblemSetMembershipResult> result = problemSetService.findMembership(10L, question.getId());

        assertThat(result).filteredOn(r -> r.id().equals(saved.getId())).extracting(ProblemSetMembershipResult::saved)
                .containsExactly(true);
        assertThat(result).filteredOn(r -> r.id().equals(notSaved.getId())).extracting(ProblemSetMembershipResult::saved)
                .containsExactly(false);
    }

    @Test
    @DisplayName("문제집에 문제를 담으면 수정 시각이 갱신된다.")
    void addItem() {
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));

        problemSetService.addItem(10L, problemSet.getId(), question.getId());

        assertThat(problemSetItemRepository.existsByProblemSetIdAndQuestionId(problemSet.getId(), question.getId())).isTrue();
    }

    @Test
    @DisplayName("이미 담긴 문제를 다시 담아도 에러 없이 무시된다.")
    void addItem_alreadyAdded() {
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));
        problemSetService.addItem(10L, problemSet.getId(), question.getId());

        problemSetService.addItem(10L, problemSet.getId(), question.getId());

        assertThat(problemSetItemRepository.findByProblemSetIdOrderByIdAsc(problemSet.getId())).hasSize(1);
    }

    @Test
    @DisplayName("존재하지 않는 문제를 담으려 하면 예외가 발생한다.")
    void addItem_questionNotFound() {
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));

        assertThatThrownBy(() -> problemSetService.addItem(10L, problemSet.getId(), 999L))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(QuestionErrorCode.QUESTION_NOT_FOUND));
    }

    @Test
    @DisplayName("문제집에서 문제를 뺀다.")
    void removeItem() {
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));
        problemSetService.addItem(10L, problemSet.getId(), question.getId());

        problemSetService.removeItem(10L, problemSet.getId(), question.getId());

        assertThat(problemSetItemRepository.existsByProblemSetIdAndQuestionId(problemSet.getId(), question.getId())).isFalse();
    }

    @Test
    @DisplayName("담겨 있지 않은 문제를 빼려 해도 에러 없이 무시된다.")
    void removeItem_notInSet() {
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));

        problemSetService.removeItem(10L, problemSet.getId(), 999L);

        assertThat(problemSetItemRepository.findByProblemSetIdOrderByIdAsc(problemSet.getId())).isEmpty();
    }

    @Test
    @DisplayName("문제집을 삭제하면 담긴 문제도 함께 삭제된다.")
    void delete() {
        Question question = questionRepository.save(QuestionFixture.rootMultipleChoice());
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));
        problemSetService.addItem(10L, problemSet.getId(), question.getId());

        problemSetService.delete(10L, problemSet.getId());

        assertThat(problemSetRepository.findById(problemSet.getId())).isEmpty();
        assertThat(problemSetItemRepository.findByProblemSetIdOrderByIdAsc(problemSet.getId())).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않거나 소유자가 다른 문제집을 삭제하면 예외가 발생한다.")
    void delete_notFound() {
        ProblemSet problemSet = problemSetRepository.save(ProblemSet.create(10L, "면접 D-7 벼락치기"));

        assertThatThrownBy(() -> problemSetService.delete(20L, problemSet.getId()))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> assertThat(((BusinessException) exception).errorCode())
                        .isEqualTo(ProblemSetErrorCode.PROBLEM_SET_NOT_FOUND));
    }
}
