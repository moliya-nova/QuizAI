package com.quizai.service;

import com.quizai.domain.ForumComment;
import com.quizai.domain.R;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ForumCommentService extends IService<ForumComment> {
    R getCommentsByPost(Long postId);
    R addComment(ForumComment comment);
    R deleteComment(Long commentId, Long userId);
}
