package com.tanhua.admin.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import com.tanhua.admin.service.AdminService;
import com.tanhua.commons.utils.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/system/users")
public class SystemController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * @Function: 功能描述 获取验证码图片
     * @Author: ChenXW
     * @Date: 11:25 2022/7/20
     */
    @GetMapping("/verification")
    public void verification(String uuid, HttpServletResponse response) throws IOException {
        //生成验证码对象
        LineCaptcha captcha = CaptchaUtil.createLineCaptcha(299, 97);
        //验证码存入Redis
        String code = captcha.getCode();
        redisTemplate.opsForValue().set(Constants.CAP_CODE+uuid,code);
        //输出验证码图片
        captcha.write(response.getOutputStream());
    }

}
