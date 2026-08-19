package com.neogul.whynago.recommendation.implement;

import com.neogul.whynago.recommendation.domain.TagWeakness;
import com.neogul.whynago.recommendation.domain.WeaknessProfile;
import com.neogul.whynago.recommendation.service.dto.WeakTagResult;
import com.neogul.whynago.recommendation.service.dto.WeakTagsResult;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

// 약점 프로필에서 화면에 보여줄 취약 태그를 고른다.
// 태그 이름까지 동점 처리에 넣는 이유는, 같은 프로필이면 항상 같은 순서가 나와야 하기 때문이다.
@Component
public class WeakTagAssembler {

    // 더 늘리면 "취약"의 의미가 흐려지고 화면도 길어진다.
    private static final int MAX_WEAK_TAGS = 4;

    public WeakTagsResult assemble(WeaknessProfile profile) {
        List<WeakTagResult> tags = profile.tagWeaknesses().stream()
                .sorted(Comparator
                        .comparingDouble(TagWeakness::weaknessScore)
                        .reversed()
                        .thenComparing(TagWeakness::name))
                .limit(MAX_WEAK_TAGS)
                .map(tag -> new WeakTagResult(tag.name(), tag.weaknessScore(), tag.sampleCount()))
                .toList();

        return new WeakTagsResult(profile.solvedCount(), tags);
    }
}
