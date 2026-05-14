package com.quizai.domain.VO;

import com.quizai.domain.Question;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WrongQuestionVO extends Question {

    private Integer wrongId;
}
