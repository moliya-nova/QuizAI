package com.quizai.controller;

import com.quizai.domain.Admin;
import com.quizai.domain.R;
import com.quizai.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("login")
    public R login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }
}
