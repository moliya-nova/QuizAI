package com.quizai.agent.dto;

import lombok.Data;

@Data
public class AiConfigDTO {
    private String llmProvider;
    private String llmApiKey;
    private String llmBaseUrl;
    private String llmModel;
    private Integer chunkSize;
    private Integer chunkOverlap;
    private String systemPrompt;
}
