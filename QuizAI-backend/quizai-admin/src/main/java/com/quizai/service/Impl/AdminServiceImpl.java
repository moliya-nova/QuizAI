package com.quizai.service.Impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.quizai.domain.Admin;
import com.quizai.domain.R;
import com.quizai.mapper.AdminMapper;
import com.quizai.service.AdminService;
import com.quizai.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public R login(Admin admin) {
        QueryWrapper<Admin> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username", admin.getUsername());
        Admin a = adminMapper.selectOne(queryWrapper);
        if (a == null) {
            return R.error("账号不存在");
        }
        if (!a.getPassword().equals(admin.getPassword())) {
            return R.error("密码错误");
        }

        String token = jwtUtils.generateToken(a.getId(), a.getUsername());
        a.setPassword(null);

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("adminInfo", a);
        return R.success("登录成功", data);
    }
}
