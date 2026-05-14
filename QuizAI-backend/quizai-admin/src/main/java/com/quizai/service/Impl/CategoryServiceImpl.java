package com.quizai.service.Impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.Category;
import com.quizai.mapper.CategoryMapper;
import com.quizai.service.CategoryService;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

}
