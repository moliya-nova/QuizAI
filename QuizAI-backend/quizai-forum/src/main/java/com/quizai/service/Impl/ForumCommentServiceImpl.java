package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.ForumComment;
import com.quizai.domain.ForumPost;
import com.quizai.domain.R;
import com.quizai.domain.VO.ForumCommentVO;
import com.quizai.mapper.ForumCommentMapper;
import com.quizai.mapper.ForumPostMapper;
import com.quizai.service.ForumCommentService;
import com.quizai.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ForumCommentServiceImpl extends ServiceImpl<ForumCommentMapper, ForumComment> implements ForumCommentService {

    @Autowired
    private ForumCommentMapper forumCommentMapper;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Override
    public R getCommentsByPost(Long postId) {
        List<ForumCommentVO> comments = forumCommentMapper.selectCommentsByPostId(postId);
        return R.success(comments);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R addComment(ForumComment comment) {
        // 敏感词过滤
        comment.setContent(sensitiveWordService.filter(comment.getContent()));
        comment.setStatus(1);
        comment.setDeleted(0);
        this.save(comment);
        forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                .eq("id", comment.getPostId())
                .setSql("comment_count = comment_count + 1"));
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deleteComment(Long commentId, Long userId) {
        ForumComment comment = this.getById(commentId);
        if (comment == null) {
            return R.error("评论不存在");
        }
        if (!comment.getUserId().equals(userId)) {
            return R.error("只能删除自己的评论");
        }
        // 逻辑删除（@TableLogic 注解会自动处理）
        this.removeById(commentId);
        forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                .eq("id", comment.getPostId())
                .setSql("comment_count = comment_count - 1"));
        return R.success();
    }
}
