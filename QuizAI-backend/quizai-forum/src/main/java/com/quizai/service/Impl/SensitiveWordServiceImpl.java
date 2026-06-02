package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.R;
import com.quizai.domain.SensitiveWord;
import com.quizai.mapper.SensitiveWordMapper;
import com.quizai.service.SensitiveWordService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

@Service
public class SensitiveWordServiceImpl extends ServiceImpl<SensitiveWordMapper, SensitiveWord> implements SensitiveWordService {

    private final Set<String> sensitiveWordSet = new CopyOnWriteArraySet<>();

    @PostConstruct
    public void init() {
        refreshCache();
    }

    private void refreshCache() {
        List<SensitiveWord> words = this.list();
        sensitiveWordSet.clear();
        sensitiveWordSet.addAll(words.stream().map(SensitiveWord::getWord).collect(Collectors.toSet()));
    }

    @Override
    public String filter(String text) {
        if (!StringUtils.hasText(text) || sensitiveWordSet.isEmpty()) {
            return text;
        }
        String result = text;
        for (String word : sensitiveWordSet) {
            if (result.contains(word)) {
                String replacement = "*".repeat(word.length());
                result = result.replace(word, replacement);
            }
        }
        return result;
    }

    @Override
    public R addWord(SensitiveWord word) {
        QueryWrapper<SensitiveWord> wrapper = new QueryWrapper<>();
        wrapper.eq("word", word.getWord());
        if (this.count(wrapper) > 0) {
            return R.error("该敏感词已存在");
        }
        this.save(word);
        refreshCache();
        return R.success();
    }

    @Override
    public R deleteWord(Long id) {
        this.removeById(id);
        refreshCache();
        return R.success();
    }

    @Override
    public R listWords() {
        return R.success(this.list());
    }
}
