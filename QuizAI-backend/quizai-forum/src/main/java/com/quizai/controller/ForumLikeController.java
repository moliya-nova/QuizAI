package com.quizai.controller;

import com.quizai.domain.ForumLike;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.service.ForumLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forum/like")
public class ForumLikeController {

    @Autowired
    private ForumLikeService forumLikeService;

    @PostMapping("toggle")
    public R toggle(@RequestBody ForumLike like) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumLikeService.toggleLike(userId, like.getPostId());
    }
}
