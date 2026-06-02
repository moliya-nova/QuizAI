package com.quizai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quizai.domain.ForumPost;
import com.quizai.domain.VO.ForumPostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ForumPostMapper extends BaseMapper<ForumPost> {

    /**
     * 分页查询帖子列表（带用户信息）
     */
    IPage<ForumPostVO> selectPostListWithUserPage(Page<ForumPostVO> page,
                                                  @Param("userId") Long userId,
                                                  @Param("status") Integer status,
                                                  @Param("type") Integer type,
                                                  @Param("keyword") String keyword);

    /**
     * 查询帖子详情
     */
    ForumPostVO selectPostDetailById(@Param("postId") Long postId,
                                     @Param("userId") Long userId);

    /**
     * 浏览量 +1
     */
    void incrementViewCount(@Param("postId") Long postId);
}
