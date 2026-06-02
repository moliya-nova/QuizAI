package com.quizai.component.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quizai.component.vo.DailyPracticeVO;
import com.quizai.domain.PracticeRecord;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PracticeHeatmapMapper extends BaseMapper<PracticeRecord> {

    @Select("SELECT LEFT(create_time, 10) AS date, SUM(total_count) AS count " +
            "FROM practice_record " +
            "WHERE user_id = #{userId} " +
            "AND LEFT(create_time, 10) >= #{startDate} " +
            "GROUP BY LEFT(create_time, 10) " +
            "ORDER BY date ASC")
    List<DailyPracticeVO> selectDailyPracticeCount(@Param("userId") Long userId,
                                                   @Param("startDate") String startDate);
}
