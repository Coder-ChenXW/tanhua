package com.tanhua.server.controller;


import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.service.CommentsService;
import com.tanhua.server.service.MovementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/movements")
public class MovementController {

    @Autowired
    private MovementService movementService;

    @Autowired
    private CommentsService commentsService;

    /**
     * @Function: 功能描述 发布动态
     * @Author: ChenXW
     * @Date: 16:56 2022/7/17
     */
    @PostMapping
    public ResponseEntity movements(Movement movement,
                                    MultipartFile imageContent[]) throws IOException {
        movementService.publishMovement(movement, imageContent);
        return ResponseEntity.ok(null);
    }


    /**
     * @Function: 功能描述 查询我的动态
     * @Author: ChenXW
     * @Date: 17:33 2022/7/17
     */
    @GetMapping("/all")
    public ResponseEntity findByUserId(Long userId,
                                       @RequestParam(defaultValue = "1") Integer page,
                                       @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findByUserId(userId, page, pagesize);
        return ResponseEntity.ok(pr);
    }


    /**
     * @Function: 功能描述 查询好友动态
     * @Author: ChenXW
     * @Date: 18:00 2022/7/17
     */
    @GetMapping
    public ResponseEntity movements(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findFriendMovements(page, pagesize);
        return ResponseEntity.ok(pr);
    }


    /**
     * @Function: 功能描述 查询推荐动态
     * @Author: ChenXW
     * @Date: 18:30 2022/7/17
     */
    @GetMapping("/recommend")
    public ResponseEntity recommend(@RequestParam(defaultValue = "1") Integer page,
                                    @RequestParam(defaultValue = "10") Integer pagesize) {
        PageResult pr = movementService.findRecommendMovements(page, pagesize);
        return ResponseEntity.ok(pr);
    }

    /**
     * @Function: 功能描述 查询单条动态
     * @Author: ChenXW
     * @Date: 19:04 2022/7/17
     */
    @GetMapping("/{id}")
    public ResponseEntity findById(@PathVariable("id") String movementId) {
        MovementsVo vo = movementService.findById(movementId);
        return ResponseEntity.ok(vo);
    }


    /**
     * @Function: 功能描述 点赞
     * @Author: ChenXW
     * @Date: 20:37 2022/7/17
     */
    @GetMapping("/{id}/like")
    public ResponseEntity like(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.likeComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * @Function: 功能描述 取消点赞
     * @Author: ChenXW
     * @Date: 21:03 2022/7/17
     */
    @GetMapping("/{id}/dislike")
    public ResponseEntity dislike(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.dislikeComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * @Function: 功能描述 喜欢
     * @Author: ChenXW
     * @Date: 21:41 2022/7/17
     */
    @GetMapping("/{id}/love")
    public ResponseEntity love(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.loveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * @Function: 功能描述 取消喜欢
     * @Author: ChenXW
     * @Date: 21:42 2022/7/17
     */
    @GetMapping("/{id}/unlove")
    public ResponseEntity unlove(@PathVariable("id") String movementId) {
        Integer likeCount = commentsService.disloveComment(movementId);
        return ResponseEntity.ok(likeCount);
    }

    /**
     * @Function: 功能描述 谁看过我
     * @Author: ChenXW
     * @Date: 18:53 2022/7/18
     */
    @GetMapping("visitors")
    public ResponseEntity queryVisitorsList() {
        List<VisitorsVo> list = movementService.queryVisitorsList();
        return ResponseEntity.ok(list);
    }
}
