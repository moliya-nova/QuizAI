package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.ForumFavorite;
import com.quizai.domain.ForumPost;
import com.quizai.domain.R;
import com.quizai.mapper.ForumFavoriteMapper;
import com.quizai.mapper.ForumPostMapper;
import com.quizai.service.ForumFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ForumFavoriteServiceImpl extends ServiceImpl<ForumFavoriteMapper, ForumFavorite> implements ForumFavoriteService {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R toggleFavorite(Long userId, Long postId) {
        QueryWrapper<ForumFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("post_id", postId);
        ForumFavorite existing = this.getOne(wrapper);

        Map<String, Object> result = new HashMap<>();
        if (existing != null) {
            // 取消收藏
            this.remove(wrapper);
            forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                    .eq("id", postId)
                    .setSql("favorite_count = GREATEST(favorite_count - 1, 0)"));
            result.put("favorited", false);
        } else {
            // 收藏
            ForumFavorite fav = new ForumFavorite();
            fav.setUserId(userId);
            fav.setPostId(postId);
            this.save(fav);
            forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                    .eq("id", postId)
                    .setSql("favorite_count = favorite_count + 1"));
            result.put("favorited", true);
        }
        ForumPost post = forumPostMapper.selectById(postId);
        result.put("favoriteCount", post != null ? post.getFavoriteCount() : 0);
        return R.success(result);
    }

    @Override
    public R getMyFavorites(Long userId, Integer page, Integer size) {
        Page<ForumFavorite> favPage = new Page<>(page, size);
        QueryWrapper<ForumFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        this.page(favPage, wrapper);

        List<Long> postIds = favPage.getRecords().stream()
                .map(ForumFavorite::getPostId)
                .collect(Collectors.toList());
        Page<ForumPost> result = new Page<>(page, size, favPage.getTotal());
        if (!postIds.isEmpty()) {
            List<ForumPost> posts = forumPostMapper.selectBatchIds(postIds);
            result.setRecords(posts);
        } else {
            result.setRecords(new ArrayList<>());
        }
        return R.success(result);
    }
}
