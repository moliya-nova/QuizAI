package com.quizai.domain;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeRecord {
    private Integer id;
    private Integer userId;
    private String subject;
    private Long totalCount;
    private Long correctCount;
    private BigDecimal accuracyRate;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String createTime;
}
