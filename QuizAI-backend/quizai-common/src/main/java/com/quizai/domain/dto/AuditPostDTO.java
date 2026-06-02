package com.quizai.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 帖子审核请求 DTO
 */
@Data
public class AuditPostDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long id;

    @NotNull(message = "审核状态不能为空")
    private Integer status;

    @Size(max = 200, message = "拒绝原因不能超过200字")
    private String rejectReason;
}
