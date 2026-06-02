package com.quizai.service;

import com.quizai.domain.ForumFavorite;
import com.quizai.domain.R;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ForumFavoriteService extends IService<ForumFavorite> {
    R toggleFavorite(Long userId, Long postId);
    R getMyFavorites(Long userId, Integer page, Integer size);
}
