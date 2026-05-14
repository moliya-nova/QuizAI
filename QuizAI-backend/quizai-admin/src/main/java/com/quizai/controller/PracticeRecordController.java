package com.quizai.controller;

import com.quizai.domain.PracticeRecord;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import com.quizai.service.PracticeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/record")
public class PracticeRecordController {

    @Autowired
    private PracticeRecordService recordService;

    @PostMapping("submit")
    public R submitRecord(@RequestBody PracticeRecord record) {
        record.setUserId(SecurityUtils.getCurrentUserId().intValue());
        return recordService.submitPracticeRecord(record);
    }

    @GetMapping("stats")
    public R getStats() {
        return recordService.selectPracticeRecordById(SecurityUtils.getCurrentUserId().intValue());
    }

    @GetMapping("leaderboard")
    public R getLeaderboard() {
        return recordService.selectAllPracticeRecord();
    }
}
