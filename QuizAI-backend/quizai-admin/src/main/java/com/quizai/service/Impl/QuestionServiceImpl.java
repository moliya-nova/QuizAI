package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.Question;
import com.quizai.mapper.QuestionMapper;
import com.quizai.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {

    @Autowired
    private QuestionMapper questionMapper;
    @Override
    public List<Question> getRandomQuestions(String subject, Integer count) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();//条件构造器
        if (!subject.equals("全部")) {
            queryWrapper.eq("subject", subject);
        }


        List<Question> questions = questionMapper.selectList(queryWrapper);
        //shuffle将集合中数据进行随机打乱
        Collections.shuffle(questions);
        if (questions.size() >= count) {
            return questions.subList(0, count);
        }
        return questions;
    }


}
