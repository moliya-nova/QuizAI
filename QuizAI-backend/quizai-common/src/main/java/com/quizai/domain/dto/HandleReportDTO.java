package com.quizai.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 处理举报请求 DTO
 */
@Data
public class HandleReportDTO {

    @NotNull(message = "举报ID不能为空")
    private Long id;

    @NotNull(message = "处理状态不能为空")
    private Integer status;

    @Size(max = 200, message = "处理备注不能超过200字")
    private String handleRemark;
}
