package com.quizai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quizai.domain.VO.WrongQuestionVO;
import com.quizai.domain.WrongQuestion;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface WrongQuestionMapper extends BaseMapper<WrongQuestion> {

    List<WrongQuestionVO> selectWrongQuestionByUid(@Param("userId") Integer userId, @Param("subject") String subject);
}
