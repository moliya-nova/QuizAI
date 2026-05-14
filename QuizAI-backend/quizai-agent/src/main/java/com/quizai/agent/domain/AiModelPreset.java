package com.quizai.agent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("ai_model_preset")
public class AiModelPreset implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String provider;

    private String protocol;

    @JsonProperty("openai_model")
    private String openaiModel;

    @JsonProperty("openai_base_url")
    private String openaiBaseUrl;

    @JsonProperty("anthropic_model")
    private String anthropicModel;

    @JsonProperty("anthropic_base_url")
    private String anthropicBaseUrl;

    @JsonProperty("ollama_model")
    private String ollamaModel;

    @JsonProperty("ollama_base_url")
    private String ollamaBaseUrl;

    @JsonProperty("api_key")
    private String apiKey;

    private String icon;

    @JsonProperty("sort_order")
    private Integer sortOrder;

    private String status;

    private String remark;

    @TableField("create_by")
    private String createBy;

    @TableField("create_time")
    private LocalDateTime createTime;

    @TableField("update_by")
    private String updateBy;

    @TableField("update_time")
    private LocalDateTime updateTime;
}
