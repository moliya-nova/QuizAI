package com.quizai.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PracticeRecordVO {
    private Integer totalPractice;
    private BigDecimal accuracy;//平均正确率
    private BigDecimal[] rendData;//近十次正确率
}
