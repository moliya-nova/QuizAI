package com.quizai.domain.VO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPracticeRecordVO {
    private String userid;
    private String name;
    private String avatar;
    private Integer totalPractice;

}
