package com.quizai.agent.dto;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String threadId;
    private Long userId;
}
