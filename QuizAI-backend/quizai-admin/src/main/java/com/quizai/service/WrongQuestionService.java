package com.quizai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quizai.domain.R;
import com.quizai.domain.WrongQuestion;

public interface WrongQuestionService extends IService<WrongQuestion> {
    // 只写方法签名，不写实现
    R selectUserWrongQuestion(Integer userId, String subject);
}