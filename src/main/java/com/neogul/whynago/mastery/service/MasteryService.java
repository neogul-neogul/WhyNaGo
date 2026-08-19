package com.neogul.whynago.mastery.service;

import com.neogul.whynago.mastery.implement.MasteryAssembler;
import com.neogul.whynago.mastery.implement.MasteryRecordAppender;
import com.neogul.whynago.mastery.service.dto.MasteryResult;
import com.neogul.whynago.mastery.service.dto.RecordMasteryCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MasteryService {

    private final MasteryRecordAppender masteryRecordAppender;
    private final MasteryAssembler masteryAssembler;

    @Transactional
    public void record(RecordMasteryCommand command) {
        masteryRecordAppender.append(command);
    }

    @Transactional(readOnly = true)
    public MasteryResult getMastery(Long userId) {
        return masteryAssembler.assemble(userId);
    }
}
