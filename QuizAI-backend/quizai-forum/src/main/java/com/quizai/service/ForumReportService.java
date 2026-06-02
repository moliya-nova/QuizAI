package com.quizai.service;

import com.quizai.domain.ForumReport;
import com.quizai.domain.R;
import com.baomidou.mybatisplus.extension.service.IService;

public interface ForumReportService extends IService<ForumReport> {
    R createReport(ForumReport report);
    R getReportList(Integer page, Integer size, Integer status);
    R handleReport(Long reportId, Integer status, String handleRemark);
}
