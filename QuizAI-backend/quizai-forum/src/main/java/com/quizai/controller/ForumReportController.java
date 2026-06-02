package com.quizai.controller;

import com.quizai.domain.ForumReport;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.service.ForumReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/forum/report")
public class ForumReportController {

    @Autowired
    private ForumReportService forumReportService;

    @PostMapping("create")
    public R create(@RequestBody ForumReport report) {
        Long userId = SecurityUtils.getCurrentUserId();
        report.setUserId(userId);
        return forumReportService.createReport(report);
    }
}
