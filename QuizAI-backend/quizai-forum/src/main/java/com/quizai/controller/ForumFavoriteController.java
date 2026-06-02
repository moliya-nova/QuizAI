package com.quizai.controller;

import com.quizai.domain.ForumFavorite;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.service.ForumFavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forum/favorite")
public class ForumFavoriteController {

    @Autowired
    private ForumFavoriteService forumFavoriteService;

    @PostMapping("toggle")
    public R toggle(@RequestBody ForumFavorite fav) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumFavoriteService.toggleFavorite(userId, fav.getPostId());
    }

    @GetMapping("my")
    public R my(@RequestParam(defaultValue = "1") Integer page,
                @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumFavoriteService.getMyFavorites(userId, page, size);
    }
}
