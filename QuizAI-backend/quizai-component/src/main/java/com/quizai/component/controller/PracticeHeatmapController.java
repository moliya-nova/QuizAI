package com.quizai.component.controller;

import com.quizai.component.service.PracticeHeatmapService;
import com.quizai.component.vo.DailyPracticeVO;
import com.quizai.domain.R;
import com.quizai.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/component/heatmap")
@RequiredArgsConstructor
public class PracticeHeatmapController {

    private final PracticeHeatmapService practiceHeatmapService;

    @GetMapping
    public R<List<DailyPracticeVO>> getHeatmap() {
        Long userId = SecurityUtils.getCurrentUserId();
        if (userId == null) {
            return R.error("用户未登录");
        }
        List<DailyPracticeVO> data = practiceHeatmapService.getDailyPracticeHeatmap(userId);
        return R.success(data);
    }
}
