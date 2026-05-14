package com.quizai.agent.service;

import com.quizai.agent.dto.AiConfigDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class AgentConfigService {

    private final WebClient webClient;

    public AgentConfigService(@Qualifier("agentWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public String getConfig() {
        return webClient.get()
                .uri("/config")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String updateConfig(AiConfigDTO config) {
        return webClient.put()
                .uri("/config")
                .bodyValue(config)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getActiveThreads() {
        return webClient.get()
                .uri("/chat/threads")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String clearAllMemory() {
        return webClient.delete()
                .uri("/chat/memory")
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String deleteThread(String threadId) {
        return webClient.delete()
                .uri("/chat/thread/{threadId}", threadId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
