package com.tanhua.server.controller;

import com.tanhua.commons.utils.JwtUtils;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.server.service.UserInfoService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


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
        boolean verifyToken = JwtUtils.verifyToken(token);
        if (!verifyToken) {
            return ResponseEntity.status(401).body(null);
        }
        Claims claims = JwtUtils.getClaims(token);
        Integer id = (Integer) claims.get("id");
        userInfo.setId(Long.valueOf(id));

        userInfoService.save(userInfo);
        return ResponseEntity.ok(null);
    }


}
