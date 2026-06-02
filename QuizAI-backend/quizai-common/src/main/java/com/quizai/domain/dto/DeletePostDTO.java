package com.quizai.domain.dto;

import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 删除帖子请求 DTO
 */
@Data
public class DeletePostDTO {

    @NotNull(message = "帖子ID不能为空")
    private Long id;
}
