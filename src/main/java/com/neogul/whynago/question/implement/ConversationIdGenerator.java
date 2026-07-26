package com.neogul.whynago.question.implement;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ConversationIdGenerator {

    public String generate() {
        return UUID.randomUUID().toString();
    }
}
