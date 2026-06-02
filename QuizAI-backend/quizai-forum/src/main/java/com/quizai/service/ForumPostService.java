package com.quizai.service;

import com.quizai.domain.ForumPost;
import com.quizai.domain.R;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ForumPostService extends IService<ForumPost> {
    R getPostList(Long currentUserId, Integer page, Integer size, Integer status, Integer type, String keyword);
    R getPostDetail(Long postId, Long currentUserId);
    R createPost(ForumPost post);
    R deletePost(Long postId, Long userId);
    R auditPost(Long postId, Integer status, String rejectReason);
    R getMyPosts(Long userId, Integer page, Integer size);
}
