package com.quizai.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.R;
import com.quizai.domain.VO.WrongQuestionVO;
import com.quizai.domain.WrongQuestion;
import com.quizai.mapper.WrongQuestionMapper;
import com.quizai.service.WrongQuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WrongQuestionServiceImpl extends ServiceImpl<WrongQuestionMapper, WrongQuestion> implements WrongQuestionService {

    @Autowired
    private WrongQuestionMapper wrongQuestionMapper;

    @Override
    public R selectUserWrongQuestion(Integer userId, String subject) {
        // 1. 查询数据库，获取错题列表
        List<WrongQuestionVO> list = wrongQuestionMapper.selectWrongQuestionByUid(userId, subject);

        // 2. 正确返回数据给前端
        return R.success(list);
    }
}