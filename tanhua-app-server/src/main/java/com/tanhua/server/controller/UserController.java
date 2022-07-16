package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * @Function: 功能描述 保存用户信息
     * @Author: ChenXW
     * @Date: 13:26 2022/7/16
     */
    @PostMapping("/loginReginfo")
    public ResponseEntity loginReinfo(@RequestBody UserInfo userInfo, @RequestHeader("Authorization") String token) {

        userInfo.setId(UserHolder.getUserId());

        userInfoService.save(userInfo);
        return ResponseEntity.ok(null);
    }


    /**
     * @Function: 功能描述 上传用户头像
     * @Author: ChenXW
     * @Date: 14:01 2022/7/16
     */
    @PostMapping("/loginReginfo/head")
    public ResponseEntity head(MultipartFile headPhoto,@RequestHeader("Authorization") String token) throws IOException {
        //向userinfo获取用户id


        userInfoService.updateHead(headPhoto,UserHolder.getUserId());
        return ResponseEntity.ok(null);

    }
}
