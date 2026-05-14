package com.quizai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quizai.domain.PracticeRecord;
import com.quizai.domain.R;

public interface PracticeRecordService extends IService<PracticeRecord> {

    R submitPracticeRecord(PracticeRecord practiceRecord);

    R selectPracticeRecordById(Integer userId);

    R selectAllPracticeRecord();
}
