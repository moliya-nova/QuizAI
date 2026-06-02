package com.quizai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quizai.domain.ForumComment;
import com.quizai.domain.VO.ForumCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ForumCommentMapper extends BaseMapper<ForumComment> {
    List<ForumCommentVO> selectCommentsByPostId(@Param("postId") Long postId);
}
