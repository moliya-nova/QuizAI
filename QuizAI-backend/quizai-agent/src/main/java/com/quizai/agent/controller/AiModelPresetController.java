package com.quizai.agent.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quizai.agent.domain.AiModelPreset;
import com.quizai.agent.service.IAiModelPresetService;
import com.quizai.domain.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("api/agent/preset")
public class AiModelPresetController {

    @Autowired
    private IAiModelPresetService presetService;

    @GetMapping("/list")
    public R<Page<AiModelPreset>> list(AiModelPreset preset,
                                        @RequestParam(defaultValue = "1") Integer pageNum,
                                        @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<AiModelPreset> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<AiModelPreset> wrapper = new LambdaQueryWrapper<>();
        if (preset.getName() != null && !preset.getName().isEmpty()) {
            wrapper.like(AiModelPreset::getName, preset.getName());
        }
        if (preset.getProvider() != null && !preset.getProvider().isEmpty()) {
            wrapper.eq(AiModelPreset::getProvider, preset.getProvider());
        }
        if (preset.getStatus() != null && !preset.getStatus().isEmpty()) {
            wrapper.eq(AiModelPreset::getStatus, preset.getStatus());
        }
        wrapper.orderByAsc(AiModelPreset::getSortOrder);
        return R.success(presetService.page(page, wrapper));
    }

    @GetMapping("/{id}")
    public R<AiModelPreset> getInfo(@PathVariable Long id) {
        return R.success(presetService.getById(id));
    }

    @PostMapping
    public R<String> add(@RequestBody AiModelPreset preset) {
        presetService.save(preset);
        return R.success("新增成功");
    }

    @PutMapping
    public R<String> edit(@RequestBody AiModelPreset preset) {
        presetService.updateById(preset);
        return R.success("修改成功");
    }

    @DeleteMapping("/{ids}")
    public R<String> remove(@PathVariable Long[] ids) {
        presetService.removeBatchByIds(Arrays.asList(ids));
        return R.success("删除成功");
    }
}
