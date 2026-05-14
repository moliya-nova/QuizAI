package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.PracticeRecord;
import com.quizai.domain.R;
import com.quizai.domain.VO.PracticeRecordVO;
import com.quizai.domain.VO.UserPracticeRecordVO;
import com.quizai.mapper.PracticeRecordMapper;
import com.quizai.service.PracticeRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PracticeRecordServiceImpl extends ServiceImpl<PracticeRecordMapper, PracticeRecord> implements PracticeRecordService {

    @Autowired
    private PracticeRecordMapper practiceRecordMapper;
    @Override
    public R submitPracticeRecord(PracticeRecord practiceRecord) {
        BigDecimal b1 = BigDecimal.valueOf(practiceRecord.getCorrectCount());
        BigDecimal b2 = BigDecimal.valueOf(practiceRecord.getTotalCount());

        if (b2.compareTo(BigDecimal.ZERO) == 0) {
            return R.error("总题数不能为0");
        }

        BigDecimal rate = b1.divide(b2, 2, BigDecimal.ROUND_HALF_UP);
        practiceRecord.setAccuracyRate(rate);
        boolean flag = this.save(practiceRecord);

        return flag ? R.success("提交成功") : R.error("提交失败");
    }

    @Override
    public R selectPracticeRecordById(Integer userId) {
        QueryWrapper<PracticeRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId);
        Page<PracticeRecord> page = new Page<>(1, 10);
        baseMapper.selectPage(page, queryWrapper);

        if (page.getRecords().size() == 0) {
            PracticeRecordVO practiceRecordVO = new PracticeRecordVO(0, BigDecimal.ZERO, null);
            return R.success(practiceRecordVO);
        }

        int totalPractice = 0;
        BigDecimal accuracy = new BigDecimal(0);
        BigDecimal[] trendData = new BigDecimal[page.getRecords().size()];

        for (int i = 0; i < page.getRecords().size(); i++) {
            totalPractice += page.getRecords().get(i).getTotalCount();
            accuracy = accuracy.add(page.getRecords().get(i).getAccuracyRate());
            if (i < 10) {
                trendData[i] = page.getRecords().get(i).getAccuracyRate();
            }
        }

        accuracy = accuracy.divide(new BigDecimal(page.getRecords().size()), 2, BigDecimal.ROUND_HALF_UP);
        accuracy = accuracy.multiply(new BigDecimal(100));

        return R.success(new PracticeRecordVO(totalPractice, accuracy, trendData));
    }

    @Override
    public R selectAllPracticeRecord() {
        List<UserPracticeRecordVO> list = practiceRecordMapper.selectUserPracticeRecord();
        return R.success(list);
    }

}
