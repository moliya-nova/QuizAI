package com.quizai.agent.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.agent.domain.AiModelPreset;
import com.quizai.agent.mapper.AiModelPresetMapper;
import com.quizai.agent.service.IAiModelPresetService;
import org.springframework.stereotype.Service;

@Service
public class AiModelPresetServiceImpl extends ServiceImpl<AiModelPresetMapper, AiModelPreset>
        implements IAiModelPresetService {
}
