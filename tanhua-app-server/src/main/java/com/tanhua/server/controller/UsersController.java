package com.tanhua.server.controller;

import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.interceptor.UserHolder;
import com.tanhua.server.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
public class UsersController {

    @Autowired
    private UserInfoService userInfoService;

    /**
     * @Function: 功能描述 查询用户资料
     * @Author: ChenXW
     * @Date: 14:42 2022/7/16
     */
    @GetMapping
    public ResponseEntity users(@RequestHeader("Authorization") String token, Long userID) {

        if (userID == null) {
            userID = UserHolder.getUserId();
        }
        UserInfoVo userInfo = userInfoService.findById(userID);
        return ResponseEntity.ok(userInfo);
    }


    /**
     * @Function: 功能描述 更新用户资料
     * @Author: ChenXW
     * @Date: 15:05 2022/7/16
     */
    @PutMapping
    public ResponseEntity updateUserInfo(@RequestBody UserInfo userInfo,@RequestHeader("Authorization") String token){

        //设置用户id
        userInfo.setId(UserHolder.getUserId());
        userInfoService.update(userInfo);
        return ResponseEntity.ok(null);
    }
}
