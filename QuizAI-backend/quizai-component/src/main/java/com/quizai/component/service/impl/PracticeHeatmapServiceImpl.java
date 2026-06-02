package com.quizai.component.service.impl;

import com.quizai.component.mapper.PracticeHeatmapMapper;
import com.quizai.component.service.PracticeHeatmapService;
import com.quizai.component.vo.DailyPracticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PracticeHeatmapServiceImpl implements PracticeHeatmapService {

    private final PracticeHeatmapMapper practiceHeatmapMapper;

    @Override
    public List<DailyPracticeVO> getDailyPracticeHeatmap(Long userId) {
        // 查询最近一年的练习数据
        String startDate = LocalDate.now().minusYears(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        return practiceHeatmapMapper.selectDailyPracticeCount(userId, startDate);
    }
}
