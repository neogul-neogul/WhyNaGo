package com.neogul.whynago.mastery.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.neogul.whynago.common.domain.MasteryLevel;
import com.neogul.whynago.fixture.TagFixture;
import com.neogul.whynago.mastery.domain.MasterySource;
import com.neogul.whynago.mastery.domain.MasteryRecord;
import com.neogul.whynago.mastery.domain.UserTagMastery;
import com.neogul.whynago.mastery.infra.MasteryRecordRepository;
import com.neogul.whynago.mastery.infra.UserTagMasteryRepository;
import com.neogul.whynago.mastery.service.dto.CategoryMasteryResult;
import com.neogul.whynago.mastery.service.dto.RecordMasteryCommand;
import com.neogul.whynago.mastery.service.dto.TagMasteryResult;
import com.neogul.whynago.question.domain.Category;
import com.neogul.whynago.question.domain.Tag;
import com.neogul.whynago.question.infra.TagRepository;
import com.neogul.whynago.support.IntegrationTestSupport;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class MasteryServiceTest extends IntegrationTestSupport {

    private static final Long USER_ID = 10L;
    private static final int MAIN_TURN = 1;
    private static final int FOLLOWUP_TURN = 2;
    private static final Long QUESTION_ID = 100L;

    @Autowired
    private MasteryService masteryService;

    @Autowired
    private MasteryRecordRepository masteryRecordRepository;

    @Autowired
    private UserTagMasteryRepository userTagMasteryRepository;

    @Autowired
    private TagRepository tagRepository;

    @Test
    @DisplayName("태그가 여러 개인 문항의 판정은 태그마다 이력과 현재값을 남긴다.")
    void record_perTag() {
        // given
        Tag index = tagRepository.save(TagFixture.db("인덱스"));
        Tag plan = tagRepository.save(TagFixture.db("실행 계획"));

        // when
        masteryService.record(command(List.of(index.getId(), plan.getId()), MasteryLevel.WEAK, "근거"));

        // then
        assertThat(masteryRecordRepository.findAll()).hasSize(2)
                .allSatisfy(record -> {
                    assertThat(record.getCategory()).isEqualTo(Category.DB);
                    assertThat(record.getSource()).isEqualTo(MasterySource.AI_ESSAY);
                });
        assertThat(userTagMasteryRepository.findByUserId(USER_ID))
                .extracting(UserTagMastery::getTagId)
                .containsExactlyInAnyOrder(index.getId(), plan.getId());
    }

    @Test
    @DisplayName("같은 태그를 다시 판정하면 현재값을 누적하지 않고 덮어쓴다.")
    void record_overwritesCurrentLevel() {
        // given
        Tag tag = tagRepository.save(TagFixture.db("인덱스"));
        masteryService.record(command(List.of(tag.getId()), MasteryLevel.NOT_LEARNED, "개념이 없다"));

        // when
        masteryService.record(command(List.of(tag.getId()), MasteryLevel.SOLID, "근거까지 설명했다"));

        // then
        assertThat(userTagMasteryRepository.findByUserId(USER_ID)).singleElement()
                .satisfies(mastery -> {
                    assertThat(mastery.getLevel()).isEqualTo(MasteryLevel.SOLID);
                    assertThat(mastery.getReason()).isEqualTo("근거까지 설명했다");
                });
        // 이력은 두 판정 모두 남는다.
        assertThat(masteryRecordRepository.findAll()).hasSize(2);
    }

    @Test
    @DisplayName("태그가 없는 문항의 판정은 카테고리 신호로만 남고 태그별 현재값에는 반영되지 않는다.")
    void record_withoutTags() {
        // when
        masteryService.record(command(List.of(), MasteryLevel.GUESSED, "용어만 나열했다"));

        // then
        assertThat(masteryRecordRepository.findAll()).singleElement()
                .satisfies(record -> assertThat(record.getTagId()).isNull());
        assertThat(userTagMasteryRepository.findByUserId(USER_ID)).isEmpty();
    }

    @Test
    @DisplayName("꼬리질문 판정은 이력에만 남고 태그별 현재값을 덮어쓰지 않는다.")
    void record_followupDoesNotOverwriteCurrentLevel() {
        // given - 본질문은 잘 답했다.
        Tag tag = tagRepository.save(TagFixture.db("인덱스"));
        masteryService.record(command(List.of(tag.getId()), MasteryLevel.SOLID, "근거까지 설명했다"));

        // when - 더 깊게 파고든 꼬리질문에서는 무너졌다.
        masteryService.record(followupCommand(List.of(tag.getId()), MasteryLevel.NOT_LEARNED, "개념이 없다"));

        // then - 현재 숙련도는 본질문 판정을 유지한다.
        assertThat(userTagMasteryRepository.findByUserId(USER_ID)).singleElement()
                .satisfies(mastery -> {
                    assertThat(mastery.getLevel())
                            .as("가장 깊은 프로브가 태그의 현재 숙련도를 덮어쓰면 안 된다")
                            .isEqualTo(MasteryLevel.SOLID);
                    assertThat(mastery.getReason()).isEqualTo("근거까지 설명했다");
                });
        // 이력에는 두 판정이 모두 남아 깊이 신호를 되짚을 수 있다.
        assertThat(masteryRecordRepository.findAll())
                .extracting(MasteryRecord::getSource, MasteryRecord::getTurn)
                .containsExactlyInAnyOrder(
                        org.assertj.core.api.Assertions.tuple(MasterySource.AI_ESSAY, MAIN_TURN),
                        org.assertj.core.api.Assertions.tuple(MasterySource.AI_ESSAY_FOLLOWUP, FOLLOWUP_TURN));
    }

    @Test
    @DisplayName("판정 분포는 태그 개수만큼 부풀지 않는다.")
    void getMastery_levelCountsNotInflatedByTags() {
        // given - 태그가 3개인 문항을 한 번 풀었다.
        Tag index = tagRepository.save(TagFixture.db("인덱스"));
        Tag plan = tagRepository.save(TagFixture.db("실행 계획"));
        Tag covering = tagRepository.save(TagFixture.db("커버링 인덱스"));
        masteryService.record(
                command(List.of(index.getId(), plan.getId(), covering.getId()), MasteryLevel.SOLID, "근거"));

        // when
        List<CategoryMasteryResult> categories = masteryService.getMastery(USER_ID).categories();

        // then - 판정은 한 번이므로 1이다. 행은 태그마다 생기므로 그대로 세면 3이 된다.
        assertThat(categories).singleElement()
                .satisfies(category ->
                        assertThat(category.levelCounts()).containsEntry(MasteryLevel.SOLID, 1L));
    }

    @Test
    @DisplayName("판정 분포는 꼬리질문 턴 수만큼 부풀지 않는다.")
    void getMastery_levelCountsExcludesFollowupTurns() {
        // given - 본질문 1턴 + 꼬리질문 2턴을 한 문항에서 풀었다.
        Tag tag = tagRepository.save(TagFixture.db("인덱스"));
        masteryService.record(command(List.of(tag.getId()), MasteryLevel.SOLID, "근거까지 설명했다"));
        masteryService.record(followupCommand(List.of(tag.getId()), MasteryLevel.UNSTABLE, "근거가 흔들린다"));
        masteryService.record(followupCommand(List.of(tag.getId()), MasteryLevel.NOT_LEARNED, "개념이 없다"));

        // when
        List<CategoryMasteryResult> categories = masteryService.getMastery(USER_ID).categories();

        // then - 꼬리질문 판정은 분포에서 빠진다.
        assertThat(categories).singleElement()
                .satisfies(category -> assertThat(category.levelCounts())
                        .containsEntry(MasteryLevel.SOLID, 1L)
                        .doesNotContainKeys(MasteryLevel.UNSTABLE, MasteryLevel.NOT_LEARNED));
    }

    @Test
    @DisplayName("숙련도 조회는 카테고리별 판정 분포와 태그별 현재 숙련도를 함께 준다.")
    void getMastery() {
        // given
        Tag index = tagRepository.save(TagFixture.db("인덱스"));
        Tag tcp = tagRepository.save(TagFixture.of("TCP", Category.NETWORK));
        masteryService.record(command(List.of(index.getId()), MasteryLevel.NOT_LEARNED, "개념이 없다"));
        masteryService.record(RecordMasteryCommand.ofEssay(
                USER_ID, QUESTION_ID + 1, Category.NETWORK, List.of(tcp.getId()),
                MasteryLevel.SOLID, "핸드셰이크를 설명했다", MAIN_TURN));

        // when
        List<CategoryMasteryResult> categories = masteryService.getMastery(USER_ID).categories();

        // then
        assertThat(categories).extracting(CategoryMasteryResult::category)
                .containsExactly(Category.DB, Category.NETWORK);
        assertThat(categories.get(0).levelCounts()).containsEntry(MasteryLevel.NOT_LEARNED, 1L);
        assertThat(categories.get(0).tags())
                .extracting(TagMasteryResult::name, TagMasteryResult::level)
                .containsExactly(org.assertj.core.api.Assertions.tuple("인덱스", MasteryLevel.NOT_LEARNED));
    }

    @Test
    @DisplayName("판정이 없는 사용자는 빈 결과를 받는다.")
    void getMastery_withoutRecords() {
        assertThat(masteryService.getMastery(USER_ID).categories()).isEmpty();
    }

    @Test
    @DisplayName("판정 이력에는 근거가 함께 남는다.")
    void record_keepsReason() {
        Tag tag = tagRepository.save(TagFixture.db("인덱스"));

        masteryService.record(command(List.of(tag.getId()), MasteryLevel.UNSTABLE, "결론은 맞지만 이유가 틀렸다"));

        assertThat(masteryRecordRepository.findAll()).singleElement()
                .extracting(MasteryRecord::getReason)
                .isEqualTo("결론은 맞지만 이유가 틀렸다");
    }

    private RecordMasteryCommand command(List<Long> tagIds, MasteryLevel level, String reason) {
        return RecordMasteryCommand.ofEssay(
                USER_ID, QUESTION_ID, Category.DB, tagIds, level, reason, MAIN_TURN);
    }

    private RecordMasteryCommand followupCommand(List<Long> tagIds, MasteryLevel level, String reason) {
        return RecordMasteryCommand.ofEssay(
                USER_ID, QUESTION_ID, Category.DB, tagIds, level, reason, FOLLOWUP_TURN);
    }
}
