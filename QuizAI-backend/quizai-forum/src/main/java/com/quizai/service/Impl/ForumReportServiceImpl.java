package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.ForumPost;
import com.quizai.domain.ForumReport;
import com.quizai.domain.R;
import com.quizai.mapper.ForumPostMapper;
import com.quizai.mapper.ForumReportMapper;
import com.quizai.service.ForumReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ForumReportServiceImpl extends ServiceImpl<ForumReportMapper, ForumReport> implements ForumReportService {

    @Autowired
    private ForumPostMapper forumPostMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R createReport(ForumReport report) {
        QueryWrapper<ForumReport> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", report.getUserId())
                .eq("post_id", report.getPostId())
                .eq("status", 0);
        if (this.count(wrapper) > 0) {
            return R.error("您已举报过该帖子");
        }
        report.setStatus(0);
        this.save(report);
        return R.success("举报已提交");
    }

    @Override
    public R getReportList(Integer page, Integer size, Integer status) {
        Page<ForumReport> pageParam = new Page<>(page, size);
        QueryWrapper<ForumReport> wrapper = new QueryWrapper<>();
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        this.page(pageParam, wrapper);
        return R.success(pageParam);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleReport(Long reportId, Integer status, String handleRemark) {
        ForumReport report = this.getById(reportId);
        if (report == null) {
            return R.error("举报记录不存在");
        }
        UpdateWrapper<ForumReport> wrapper = new UpdateWrapper<>();
        wrapper.eq("id", reportId)
                .set("status", status)
                .set("handle_remark", handleRemark)
                .set("handle_time", LocalDateTime.now());
        this.update(wrapper);
        // 如果下架帖子，同步更新帖子状态
        if (status == 2) {
            forumPostMapper.update(null, new UpdateWrapper<ForumPost>()
                    .eq("id", report.getPostId())
                    .set("status", 3));
        }
        return R.success();
    }
}
