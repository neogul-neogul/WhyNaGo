package com.neogul.whynago.notification.implement;

import com.neogul.whynago.common.mail.EmailSender;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudyReminderMailSender {

    private static final String SUBJECT = "[WhyNaGo] 오늘의 학습 리마인드";
    private static final String SERVICE_URL = "https://why-na-go.vercel.app";
    private static final String BODY_TEMPLATE = readTemplate("mail/study-reminder.html");
    private static final String STATUS_ROW_TEMPLATE = readTemplate("mail/study-reminder-status-row.html");

    private final EmailSender emailSender;

    public void send(String to, boolean solvedToday, boolean interviewDoneToday) {
        emailSender.sendHtml(to, SUBJECT, buildHtmlBody(solvedToday, interviewDoneToday));
    }

    private String buildHtmlBody(boolean solvedToday, boolean interviewDoneToday) {
        return BODY_TEMPLATE
                .replace("{{solvedTodayRow}}", statusRow("오늘 문제 풀이", "스트릭 유지 여부", solvedToday))
                .replace("{{interviewDoneTodayRow}}",
                        statusRow("1일 1면접", "오늘의 면접 질문에 답했는지 여부", interviewDoneToday))
                .replace("{{serviceUrl}}", SERVICE_URL);
    }

    private String statusRow(String title, String description, boolean done) {
        String badgeBg = done ? "#e8f5ee" : "#fef4f2";
        String badgeColor = done ? "#16a34a" : "#c2410c";
        String badgeLabel = done ? "완료" : "미완료";

        return STATUS_ROW_TEMPLATE
                .replace("{{title}}", title)
                .replace("{{description}}", description)
                .replace("{{badgeBg}}", badgeBg)
                .replace("{{badgeColor}}", badgeColor)
                .replace("{{badgeLabel}}", badgeLabel);
    }

    private static String readTemplate(String path) {
        try (InputStream in = StudyReminderMailSender.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalStateException("메일 템플릿을 찾을 수 없습니다: " + path);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("메일 템플릿을 읽는 중 오류가 발생했습니다: " + path, e);
        }
    }
}
