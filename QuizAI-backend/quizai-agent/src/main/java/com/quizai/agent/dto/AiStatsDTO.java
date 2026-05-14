package com.quizai.agent.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class AiStatsDTO {
    private boolean enabled;
    private long totalRequests;
    private int activeConnections;
    private int maxConcurrency;
    private List<Map<String, Object>> activeThreads;
}
