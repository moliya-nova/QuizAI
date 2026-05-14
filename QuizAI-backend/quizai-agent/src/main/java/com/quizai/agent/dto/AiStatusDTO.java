package com.quizai.agent.dto;

import lombok.Data;

@Data
public class AiStatusDTO {
    private boolean enabled;
    private int maxConcurrency;
    private int activeConnections;
    private long totalRequests;
    private String message;
}
