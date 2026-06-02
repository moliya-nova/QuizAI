package com.quizai.service;

import com.quizai.domain.R;
import com.quizai.domain.SensitiveWord;
import com.baomidou.mybatisplus.extension.service.IService;

public interface SensitiveWordService extends IService<SensitiveWord> {
    String filter(String text);
    R addWord(SensitiveWord word);
    R deleteWord(Long id);
    R listWords();
}
