package com.tanhua.admin.controller;


import com.tanhua.admin.service.ManagerService;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/manage")
public class ManageController {

    @Autowired
    private ManagerService managerService;

    /**
     * @Function: 功能描述 分页查询用户列表
     * @Author: ChenXW
     * @Date: 19:30 2022/7/20
     */
    @GetMapping("/users")
    public ResponseEntity users(@RequestParam(defaultValue = "1") Integer page,
                                @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult result = managerService.findAllUsers(page,pagesize);
        return ResponseEntity.ok(result);
    }

    /**
     * @Function: 功能描述 根据id查询
     * @Author: ChenXW
     * @Date: 19:43 2022/7/20
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity findUserById(@PathVariable("userId") Long userId) {
        UserInfo userInfo = managerService.findUserById(userId);
        return ResponseEntity.ok(userInfo);
    }


    /** 
     * @Function: 功能描述 查询指定用户发布的所有视频列表
     * @Author: ChenXW
     * @Date: 19:48 2022/7/20
     */
    @GetMapping("/videos")
    public ResponseEntity videos(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "10") Integer pagesize,
                                 Long uid ) {
        PageResult result = managerService.findAllVideos(page,pagesize,uid);
        return ResponseEntity.ok(result);
    }

    /**
     * @Function: 功能描述 查询动态
     * @Author: ChenXW
     * @Date: 19:58 2022/7/20
     */
    @GetMapping("/messages")
    public ResponseEntity messages(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   Long uid,Integer state) {
        PageResult result = managerService.findAllMovements(page,pagesize,uid,state);
        return ResponseEntity.ok(result);
    }


    /**
     * @Function: 功能描述 用户冻结
     * @Author: ChenXW
     * @Date: 20:47 2022/7/20
     */
    @PostMapping("/users/freeze")
    public ResponseEntity freeze(@RequestBody Map params) {
        Map map =  managerService.userFreeze(params);
        return ResponseEntity.ok(map);
    }


    /**
     * @Function: 功能描述 用户解冻
     * @Author: ChenXW
     * @Date: 20:57 2022/7/20
     */
    @PostMapping("/users/unfreeze")
    public ResponseEntity unfreeze(@RequestBody  Map params) {
        Map map =  managerService.userUnfreeze(params);
        return ResponseEntity.ok(map);
    }
}
