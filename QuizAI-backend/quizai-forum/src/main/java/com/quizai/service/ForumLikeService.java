package com.quizai.service;

import com.quizai.domain.ForumLike;
import com.quizai.domain.R;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ForumLikeService extends IService<ForumLike> {
    R toggleLike(Long userId, Long postId);
}
