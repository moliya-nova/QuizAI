package com.quizai.controller;

import com.quizai.domain.R;
import com.quizai.domain.SensitiveWord;
import com.quizai.service.SensitiveWordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("api/admin/sensitive-word")
public class AdminSensitiveWordController {

    @Autowired
    private SensitiveWordService sensitiveWordService;

    @GetMapping("list")
    public R list() {
        return sensitiveWordService.listWords();
    }

    @PostMapping("add")
    public R add(@RequestBody SensitiveWord word) {
        return sensitiveWordService.addWord(word);
    }

    @PostMapping("delete")
    public R delete(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        return sensitiveWordService.deleteWord(id);
    }
}
