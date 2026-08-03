package com.neogul.whynago.learningrecord.implement;

import com.neogul.whynago.solvedsession.implement.SolvedSessionReader;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SolvedDateReader {

    private final SolvedSessionReader solvedSessionReader;

    public List<LocalDate> readAll(Long userId) {
        return solvedSessionReader.readAll(userId).stream()
                .map(session -> session.getSolvedAt().toLocalDate())
                .toList();
    }
}
