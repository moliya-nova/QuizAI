package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.ForumLike;
import com.quizai.domain.ForumPost;
import com.quizai.domain.R;
import com.quizai.mapper.ForumLikeMapper;
import com.quizai.mapper.ForumPostMapper;
import com.quizai.service.ForumLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ForumLikeServiceImpl extends ServiceImpl<ForumLikeMapper, ForumLike> implements ForumLikeService {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R toggleLike(Long userId, Long postId) {
        QueryWrapper<ForumLike> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("post_id", postId);
        ForumLike existing = this.getOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        if (existing != null) {
            // 取消点赞
            this.remove(wrapper);
            forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                    .eq("id", postId)
                    .setSql("like_count = GREATEST(like_count - 1, 0)"));
            result.put("liked", false);
        } else {
            // 点赞
            ForumLike like = new ForumLike();
            like.setUserId(userId);
            like.setPostId(postId);
            this.save(like);
            forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                    .eq("id", postId)
                    .setSql("like_count = like_count + 1"));
            result.put("liked", true);
        }
        ForumPost post = forumPostMapper.selectById(postId);
        result.put("likeCount", post != null ? post.getLikeCount() : 0);
        return R.success(result);
    }
}
