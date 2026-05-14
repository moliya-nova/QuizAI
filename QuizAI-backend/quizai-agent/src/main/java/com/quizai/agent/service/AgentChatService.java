package com.quizai.agent.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.Map;

@Slf4j
@Service
public class AgentChatService {

    private final WebClient webClient;

    public AgentChatService(@Qualifier("agentWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<ServerSentEvent<String>> streamChat(String message, String threadId, String username) {
        Map<String, Object> body = Map.of(
                "message", message,
                "thread_id", threadId,
                "username", username
        );

        return webClient.post()
                .uri("/chat/stream")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<ServerSentEvent<String>>() {})
                .onErrorResume(e -> {
                    log.error("AI 聊天流式请求异常: {}", e.getMessage());
                    ServerSentEvent<String> errorEvent = ServerSentEvent.<String>builder()
                            .event("error")
                            .data("{\"code\":500,\"msg\":\"AI 服务异常: " + e.getMessage() + "\",\"data\":\"\"}")
                            .build();
                    return Flux.just(errorEvent);
                });
    }

    public String deleteMemory(String threadId) {
        return webClient.delete()
                .uri("/chat/memory/{threadId}", threadId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getHistory(String threadId) {
        return webClient.get()
                .uri("/chat/history/{threadId}", threadId)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }
}
