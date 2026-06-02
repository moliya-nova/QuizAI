package com.quizai.controller;

import com.quizai.domain.ForumComment;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.service.ForumCommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forum/comment")
public class ForumCommentController {

    @Autowired
    private ForumCommentService forumCommentService;

    @GetMapping("list")
    public R list(@RequestParam Long postId) {
        return forumCommentService.getCommentsByPost(postId);
    }

    @PostMapping("add")
    public R add(@RequestBody ForumComment comment) {
        Long userId = SecurityUtils.getCurrentUserId();
        comment.setUserId(userId);
        return forumCommentService.addComment(comment);
    }

    @PostMapping("delete")
    public R delete(@RequestParam Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        return forumCommentService.deleteComment(id, userId);
    }
}
