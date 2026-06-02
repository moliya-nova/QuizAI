package com.quizai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quizai.domain.R;
import com.quizai.domain.VO.ForumPostVO;
import com.quizai.domain.dto.AuditPostDTO;
import com.quizai.domain.dto.DeletePostDTO;
import com.quizai.domain.dto.HandleReportDTO;
import com.quizai.mapper.ForumPostMapper;
import com.quizai.service.ForumPostService;
import com.quizai.service.ForumReportService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/admin/forum")
public class AdminForumController {

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Autowired
    private ForumReportService forumReportService;

    @GetMapping("post/list")
    public R<IPage<ForumPostVO>> postList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword) {

        Page<ForumPostVO> pageParam = new Page<>(page, size);
        IPage<ForumPostVO> result = forumPostMapper.selectPostListWithUserPage(pageParam, null, status, type, keyword);
        return R.success(result);
    }

    @PostMapping("post/audit")
    public R auditPost(@Valid @RequestBody AuditPostDTO dto) {
        return forumPostService.auditPost(dto.getId(), dto.getStatus(), dto.getRejectReason());
    }

    @PostMapping("post/delete")
    public R deletePost(@Valid @RequestBody DeletePostDTO dto) {
        forumPostService.removeById(dto.getId());
        return R.success();
    }

    @GetMapping("report/list")
    public R reportList(@RequestParam(defaultValue = "1") Integer page,
                        @RequestParam(defaultValue = "10") Integer size,
                        @RequestParam(required = false) Integer status) {
        return forumReportService.getReportList(page, size, status);
    }

    @PostMapping("report/handle")
    public R handleReport(@Valid @RequestBody HandleReportDTO dto) {
        return forumReportService.handleReport(dto.getId(), dto.getStatus(), dto.getHandleRemark());
    }
}
