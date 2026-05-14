package com.quizai.agent.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizai.agent.dto.ChatRequest;
import com.quizai.agent.service.AgentChatService;
import com.quizai.agent.service.AgentConcurrencyService;
import com.quizai.agent.service.AgentStatusService;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("api/agent/chat")
public class AgentChatController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AgentChatService agentChatService;

    @Autowired
    private AgentConcurrencyService concurrencyService;

    @Autowired
    private AgentStatusService statusService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamChat(@RequestBody ChatRequest request,
                                                     HttpServletRequest httpRequest) {
        if (!statusService.isEnabled()) {
            return errorSseEvent(503, "AI 服务已关闭");
        }

        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return errorSseEvent(401, "未授权访问");
        }

        if (!concurrencyService.tryAcquire()) {
            return errorSseEvent(429, "AI 服务繁忙，请稍后重试");
        }

        statusService.incrementRequests();
        statusService.incrementConnections();

        String username = getUsernameFromRequest(httpRequest);
        String clientThreadId = request.getThreadId() != null ? request.getThreadId() : "default";
        String threadId = "user_" + userId + "_" + clientThreadId;

        return agentChatService.streamChat(request.getMessage(), threadId, username)
                .doFinally(signal -> {
                    concurrencyService.release();
                    statusService.decrementConnections();
                });
    }

    @DeleteMapping("/memory/{threadId}")
    public R<?> deleteMemory(@PathVariable String threadId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.error("未授权访问");
        }
        String fullThreadId = "user_" + userId + "_" + threadId;
        agentChatService.deleteMemory(fullThreadId);
        return R.success("记忆已清除");
    }

    @GetMapping("/history/{threadId}")
    public R<?> getHistory(@PathVariable String threadId) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.error("未授权访问");
        }
        String fullThreadId = "user_" + userId + "_" + threadId;
        String result = agentChatService.getHistory(fullThreadId);
        try {
            JsonNode json = objectMapper.readTree(result);
            JsonNode data = json.get("data");
            return R.success(objectMapper.treeToValue(data, Object.class));
        } catch (Exception e) {
            return R.success(result);
        }
    }

    private Flux<ServerSentEvent<String>> errorSseEvent(int code, String msg) {
        ServerSentEvent<String> event = ServerSentEvent.<String>builder()
                .event("error")
                .data("{\"code\":" + code + ",\"msg\":\"" + msg + "\",\"data\":\"\"}")
                .build();
        return Flux.just(event);
    }

    private String getUsernameFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            String token = bearerToken.substring(7);
            Claims claims = jwtUtils.getClaimsByToken(token);
            if (claims != null) {
                return claims.getSubject();
            }
        }
        return "unknown";
    }
}
