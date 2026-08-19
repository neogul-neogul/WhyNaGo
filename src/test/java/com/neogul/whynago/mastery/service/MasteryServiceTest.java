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
    @DisplayName("숙련도 조회는 카테고리별 판정 분포와 태그별 현재 숙련도를 함께 준다.")
    void getMastery() {
        // given
        Tag index = tagRepository.save(TagFixture.db("인덱스"));
        Tag tcp = tagRepository.save(TagFixture.of("TCP", Category.NETWORK));
        masteryService.record(command(List.of(index.getId()), MasteryLevel.NOT_LEARNED, "개념이 없다"));
        masteryService.record(new RecordMasteryCommand(
                USER_ID, QUESTION_ID + 1, Category.NETWORK, List.of(tcp.getId()),
                MasteryLevel.SOLID, "핸드셰이크를 설명했다", MasterySource.AI_ESSAY));

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
        return new RecordMasteryCommand(
                USER_ID, QUESTION_ID, Category.DB, tagIds, level, reason, MasterySource.AI_ESSAY);
    }
}
