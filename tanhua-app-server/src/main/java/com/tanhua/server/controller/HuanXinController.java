package com.tanhua.server.controller;


import com.tanhua.model.vo.HuanXinUserVo;
import com.tanhua.server.service.HuanXinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/huanxin")
public class HuanXinController {

    @Autowired
    private HuanXinService huanXinService;

    /**
     * @Function: 功能描述 查询环信的账号密码
     * @Author: ChenXW
     * @Date: 9:39 2022/7/18
     */
    @GetMapping("/user")
    public ResponseEntity user() {
        HuanXinUserVo vo = huanXinService.findHuanXinUser();
        return ResponseEntity.ok(vo);
    }

}
