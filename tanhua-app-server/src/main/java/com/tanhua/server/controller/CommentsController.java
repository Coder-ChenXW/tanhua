package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.server.service.CommentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * @Function: 功能描述 发布评论
     * @Author: ChenXW
     * @Date: 19:24 2022/7/17
     */
    @PostMapping
    public ResponseEntity saveComments(@RequestBody Map map){
        String movementId = (String )map.get("movementId");
        String comment = (String)map.get("comment");
        commentsService.saveComments(movementId,comment);
        return ResponseEntity.ok(null);
    }
    
    /** 
     * @Function: 功能描述 分列查询评论列表
     * @Author: ChenXW
     * @Date: 20:06 2022/7/17
     */
    @GetMapping
    public ResponseEntity findComments(@RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize,
                                       String movementId) {
        PageResult pr = commentsService.findComments(movementId,page,pagesize);
        return ResponseEntity.ok(pr);
    }
}
