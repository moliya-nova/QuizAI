package com.quizai.controller;

import com.quizai.domain.Question;
import com.quizai.domain.R;
import com.quizai.service.QuestionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/question")
public class QuestionController {

    @Autowired
    private QuestionService questionService;
    //默认值

    @GetMapping("random")
    public R<List<Question>> random(@RequestParam(defaultValue="全部") String subject,
                                   @RequestParam(defaultValue="10") Integer count){
       List<Question> randomQuestions = questionService.getRandomQuestions(subject, count);
        return R.success(randomQuestions);

   }
}
