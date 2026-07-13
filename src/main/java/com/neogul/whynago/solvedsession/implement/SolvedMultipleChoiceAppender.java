package com.neogul.whynago.solvedsession.implement;

import com.neogul.whynago.solvedsession.domain.SolvedMultipleChoice;
import com.neogul.whynago.solvedsession.infra.SolvedMultipleChoiceRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedMultipleChoiceAppender {

    private final SolvedMultipleChoiceRepository solvedMultipleChoiceRepository;

    public List<SolvedMultipleChoice> appendAll(List<SolvedMultipleChoice> solvedMultipleChoices) {
        return solvedMultipleChoiceRepository.saveAll(solvedMultipleChoices);
    }
}
