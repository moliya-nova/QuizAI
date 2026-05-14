package com.quizai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quizai.domain.PracticeRecord;
import com.quizai.domain.VO.UserPracticeRecordVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PracticeRecordMapper extends BaseMapper<PracticeRecord> {
        @Select("select \n" +
                "    sum(p.total_count) total_practice, u.id user_id, u.nickname name, avatar\n" +
                "from user u join practice_record p on u.id = p.user_id\n" +
                "group by u.id, u.nickname, avatar\n"+
                "order by total_practice desc\n"+
                "limit 0,3")
        List<UserPracticeRecordVO> selectUserPracticeRecord();
    }

