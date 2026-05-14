package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.R;
import com.quizai.domain.User;
import com.quizai.domain.VO.UserVO;
import com.quizai.mapper.UserMapper;
import com.quizai.service.UserService;
import com.quizai.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private  UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public R login(User user) {
        //
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());
        User u = userMapper.selectOne(queryWrapper);
        if (u == null) { // 用户名不存在
            return R.error("用户名不存在");
        }

        // 2. 校验用户名
        if (!u.getUsername().equals(user.getUsername())) {
            return R.error("用户名错误");
        }

        // 对用户传递的密码进行加密
        String newPass = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        if (!u.getPassword().equals(newPass)) {
            return R.error("密码错误");
        }
        String token = jwtUtils.generateToken(u.getId(), u.getUsername());
// 清空密码再返回用户信息
        u.setPassword("");
        UserVO userVO = new UserVO(token, u);
        return R.success("登录成功", userVO);
    }

    @Override
    public R register(User user) {
        //判断用户是否存在
        // 1. 判断用户名是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", user.getUsername());  // 根据用户名查询
        User u = userMapper.selectOne(queryWrapper);
        if (u != null) { // 用户名已存在
            return R.error("注册失败，用户名已存在");
        }

        // 2. 将密码加密后保存到数据库
        String encryptedPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
        user.setPassword(encryptedPassword);

        int count = userMapper.insert(user);
        return count > 0 ? R.success("注册成功", null) : R.error("注册失败");
    }

    @Override
    public R changeUserInfo(User user) {
        return null;
    }


}