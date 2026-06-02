package com.quizai.controller;

import com.quizai.domain.ForumPost;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.service.ForumPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forum/post")
public class ForumPostController {

    @Autowired
    private ForumPostService forumPostService;

    @GetMapping("list")
    public R list(@RequestParam(defaultValue = "1") Integer page,
                  @RequestParam(defaultValue = "10") Integer size,
                  @RequestParam(required = false) Integer type,
                  @RequestParam(required = false) String keyword) {
        // status=1 表示只查询已审核通过的帖子
        return forumPostService.getPostList(null, page, size, 1, type, keyword);
    }

    @GetMapping("detail")
    public R detail(@RequestParam Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumPostService.getPostDetail(id, userId);
    }

    @PostMapping("create")
    public R create(@RequestBody ForumPost post) {
        Long userId = SecurityUtils.getCurrentUserId();
        post.setUserId(userId);
        return forumPostService.createPost(post);
    }

    @PostMapping("delete")
    public R delete(@RequestParam Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumPostService.deletePost(id, userId);
    }

    @GetMapping("my")
    public R my(@RequestParam(defaultValue = "1") Integer page,
                @RequestParam(defaultValue = "10") Integer size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumPostService.getMyPosts(userId, page, size);
    }
}
