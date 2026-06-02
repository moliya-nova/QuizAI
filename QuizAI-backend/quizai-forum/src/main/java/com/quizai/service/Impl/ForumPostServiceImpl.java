package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.ForumPost;
import com.quizai.domain.R;
import com.quizai.domain.VO.ForumPostVO;
import com.quizai.mapper.ForumPostMapper;
import com.quizai.service.ForumPostService;
import com.quizai.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ForumPostServiceImpl extends ServiceImpl<ForumPostMapper, ForumPost> implements ForumPostService {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @Override
    public R getPostList(Long currentUserId, Integer page, Integer size, Integer status, Integer type, String keyword) {
        Page<ForumPostVO> pageParam = new Page<>(page, size);
        IPage<ForumPostVO> result = forumPostMapper.selectPostListWithUserPage(pageParam, currentUserId, status, type, keyword);
        return R.success(result);
    }

    @Override
    public R getPostDetail(Long postId, Long currentUserId) {
        ForumPostVO vo = forumPostMapper.selectPostDetailById(postId, currentUserId);
        if (vo == null) {
            return R.error("帖子不存在");
        }
        // 浏览量 +1（异步可优化，当前简单实现）
        forumPostMapper.incrementViewCount(postId);
        return R.success(vo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createPost(ForumPost post) {
        post.setTitle(sensitiveWordService.filter(post.getTitle()));
        post.setContent(sensitiveWordService.filter(post.getContent()));
        post.setStatus(0);
        post.setLikeCount(0);
        post.setCommentCount(0);
        post.setFavoriteCount(0);
        post.setViewCount(0);
        post.setDeleted(0);
        this.save(post);
        return R.success("帖子已提交，等待审核");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R deletePost(Long postId, Long userId) {
        ForumPost post = this.getById(postId);
        if (post == null) {
            return R.error("帖子不存在");
        }
        if (!post.getUserId().equals(userId)) {
            return R.error("只能删除自己的帖子");
        }
        // 逻辑删除（@TableLogic 注解会自动处理）
        this.removeById(postId);
        return R.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R auditPost(Long postId, Integer status, String rejectReason) {
        ForumPost post = this.getById(postId);
        if (post == null) {
            return R.error("帖子不存在");
        }
        UpdateWrapper<ForumPost> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", postId).set("status", status);
        if (status == 2) {
            wrapper.set("reject_reason", rejectReason);
        } else {
            wrapper.set("reject_reason", null);
        }
        this.update(wrapper);
        return R.success();
    }

    @Override
    public R getMyPosts(Long userId, Integer page, Integer size) {
        Page<ForumPostVO> pageParam = new Page<>(page, size);
        IPage<ForumPostVO> result = forumPostMapper.selectPostListWithUserPage(pageParam, userId, null, null, null);
        return R.success(result);
    }
}
