package com.quizai.component.service;

import com.quizai.component.vo.DailyPracticeVO;

import java.util.List;

public interface PracticeHeatmapService {
    List<DailyPracticeVO> getDailyPracticeHeatmap(Long userId);
}
