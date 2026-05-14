package com.quizai.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.quizai.domain.R;
import com.quizai.domain.User;

public interface UserService extends IService<User> {
    R login(User user);
    R register(User user);
    R changeUserInfo(User user);
}
