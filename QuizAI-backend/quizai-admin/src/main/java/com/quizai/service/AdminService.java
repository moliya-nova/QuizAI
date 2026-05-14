package com.quizai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quizai.domain.Admin;
import com.quizai.domain.R;

public interface AdminService extends IService<Admin> {
    R login(Admin admin);
}
