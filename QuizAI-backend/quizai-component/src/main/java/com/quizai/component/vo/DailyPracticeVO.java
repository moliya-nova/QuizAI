package com.quizai.component.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DailyPracticeVO {
    private String date;
    private Integer count;
}
