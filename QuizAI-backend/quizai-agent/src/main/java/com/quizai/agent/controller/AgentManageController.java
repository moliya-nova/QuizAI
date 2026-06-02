package com.quizai.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizai.agent.domain.AiModelPreset;
import com.quizai.agent.dto.AiConfigDTO;
import com.quizai.agent.dto.AiStatsDTO;
import com.quizai.agent.dto.AiStatusDTO;
import com.quizai.agent.dto.ConcurrencyDTO;
import com.quizai.agent.service.AgentConfigService;
import com.quizai.agent.service.AgentConcurrencyService;
import com.quizai.agent.service.AgentStatusService;
import com.quizai.agent.service.IAiModelPresetService;
import com.quizai.domain.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("api/agent/manage")
public class AgentManageController {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private AgentStatusService statusService;

    @Autowired
    private AgentConcurrencyService concurrencyService;

    @Autowired
    private AgentConfigService configService;

    @Autowired
    private IAiModelPresetService presetService;

    @GetMapping("/status")
    public R<AiStatusDTO> getStatus() {
        AiStatusDTO status = new AiStatusDTO();
        status.setEnabled(statusService.isEnabled());
        status.setMaxConcurrency(concurrencyService.getMaxConcurrency());
        status.setActiveConnections((int) statusService.getActiveConnections());
        status.setTotalRequests(statusService.getTotalRequests());
        status.setMessage(statusService.isEnabled() ? "AI 服务运行中" : "AI 服务已关闭");
        return R.success(status);
    }

    @PutMapping("/status")
    public R<String> toggleStatus(@RequestParam(required = false) Boolean enabled) {
        if (enabled != null) {
            statusService.setEnabled(enabled);
        } else {
            statusService.toggleEnabled();
        }
        return R.success(statusService.isEnabled() ? "AI 服务已开启" : "AI 服务已关闭");
    }

    @GetMapping("/concurrency")
    public R<ConcurrencyDTO> getConcurrency() {
        ConcurrencyDTO dto = new ConcurrencyDTO();
        dto.setMaxConcurrency(concurrencyService.getMaxConcurrency());
        return R.success(dto);
    }

    @PutMapping("/concurrency")
    public R<String> setConcurrency(@RequestBody ConcurrencyDTO dto) {
        concurrencyService.setMaxConcurrency(dto.getMaxConcurrency());
        return R.success("最大并发数已设置为 " + dto.getMaxConcurrency());
    }

    @GetMapping("/config")
    public R<?> getConfig() {
        try {
            String config = configService.getConfig();
            JsonNode json = objectMapper.readTree(config);
            JsonNode data = json.get("data");
            return R.success(objectMapper.treeToValue(data, Object.class));
        } catch (Exception e) {
            return R.error("获取 AI 配置失败: " + e.getMessage());
        }
    }

    @PutMapping("/config")
    public R<String> updateConfig(@RequestBody AiConfigDTO config) {
        try {
            String result = configService.updateConfig(config);
            return R.success("AI 配置已更新", result);
        } catch (Exception e) {
            return R.error("更新 AI 配置失败: " + e.getMessage());
        }
    }

    @GetMapping("/stats")
    public R<AiStatsDTO> getStats() {
        AiStatsDTO stats = new AiStatsDTO();
        stats.setEnabled(statusService.isEnabled());
        stats.setTotalRequests(statusService.getTotalRequests());
        stats.setActiveConnections((int) statusService.getActiveConnections());
        stats.setMaxConcurrency(concurrencyService.getMaxConcurrency());
        try {
            String threads = configService.getActiveThreads();
            JsonNode json = objectMapper.readTree(threads);
            JsonNode arr = json.get("data");
            List<Map<String, Object>> threadList = new ArrayList<>();
            if (arr != null && arr.isArray()) {
                for (JsonNode item : arr) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = objectMapper.convertValue(item, Map.class);
                    threadList.add(map);
                }
            }
            stats.setActiveThreads(threadList);
        } catch (Exception e) {
            stats.setActiveThreads(java.util.Collections.emptyList());
        }
        return R.success(stats);
    }

    @GetMapping("/threads")
    public R<?> getThreads() {
        try {
            String threads = configService.getActiveThreads();
            JsonNode json = objectMapper.readTree(threads);
            JsonNode data = json.get("data");
            return R.success(objectMapper.treeToValue(data, Object.class));
        } catch (Exception e) {
            return R.error("获取会话列表失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/memory")
    public R<String> clearAllMemory() {
        try {
            String result = configService.clearAllMemory();
            return R.success("所有会话记忆已清除", result);
        } catch (Exception e) {
            return R.error("清除失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/thread/{threadId}")
    public R<String> deleteThread(@PathVariable String threadId) {
        try {
            String result = configService.deleteThread(threadId);
            return R.success("会话已删除", result);
        } catch (Exception e) {
            return R.error("删除失败: " + e.getMessage());
        }
    }

    @GetMapping("/presets")
    public R<List<AiModelPreset>> getPresets() {
        LambdaQueryWrapper<AiModelPreset> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiModelPreset::getStatus, "0")
               .orderByAsc(AiModelPreset::getSortOrder);
        return R.success(presetService.list(wrapper));
    }

    @PostMapping("/rag-rebuild")
    public R<String> ragRebuild() {
        try {
            String result = configService.ragRebuild();
            return R.success("RAG 向量库索引重建成功", result);
        } catch (Exception e) {
            log.error("RAG 索引重建失败", e);
            return R.error("RAG 索引重建失败: " + e.getMessage());
        }
    }
}
