package com.quizai.controller;

import com.quizai.domain.Category;
import com.quizai.domain.R;
import com.quizai.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("list")
    public R<List<Category>> list(){
        List<Category> list = categoryService.list();
        return R.success(list);
    }
}
