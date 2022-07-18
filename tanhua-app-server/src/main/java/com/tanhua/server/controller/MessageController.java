package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.service.MessagesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessagesService messagesService;

    /**
     * @Function: 功能描述 根据环信用户id查询用户详情
     * @Author: ChenXW
     * @Date: 9:55 2022/7/18
     */
    @GetMapping("/userinfo")
    public ResponseEntity userinfo(String huanxinId) {
        UserInfoVo vo = messagesService.findUserInfoByHuanxin(huanxinId);
        return ResponseEntity.ok(vo);
    }

    /** 
     * @Function: 功能描述 添加好友
     * @Author: ChenXW
     * @Date: 14:13 2022/7/18
     */
    @PostMapping("/contacts")
    public ResponseEntity contacts(@RequestBody Map map) {
        Long friendId = Long.valueOf(map.get("userId").toString());
        messagesService.contacts(friendId);
        return ResponseEntity.ok(null);
    }
    
    
    /** 
     * @Function: 功能描述 分页查询联系人列表
     * @Author: ChenXW
     * @Date: 14:34 2022/7/18
     */
    @GetMapping("/contacts")
    public ResponseEntity contacts(@RequestParam(defaultValue = "1") Integer page,
                                   @RequestParam(defaultValue = "10") Integer pagesize,
                                   String keyword) {
        PageResult pr = messagesService.findFriends(page,pagesize,keyword);
        return ResponseEntity.ok(pr);
    }
}
