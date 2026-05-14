package com.quizai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quizai.domain.Question;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface QuestionService extends IService<Question> {
    List<Question> getRandomQuestions(String subject, Integer count);

}
