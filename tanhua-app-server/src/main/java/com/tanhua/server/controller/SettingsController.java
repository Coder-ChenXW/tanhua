package com.tanhua.server.controller;

import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("/users")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    /**
     * @Function: 功能描述 查询通用设置
     * @Author: ChenXW
     * @Date: 17:52 2022/7/16
     */
    @GetMapping("/settings")
    public ResponseEntity settings() {
        SettingsVo vo = settingsService.settings();
        return ResponseEntity.ok(vo);
    }


    /**
     * @Function: 功能描述 设置陌生人问题
     * @Author: ChenXW
     * @Date: 18:09 2022/7/16
     */
    @PostMapping("/questions")
    public ResponseEntity questions(@RequestBody Map map) {
        String content = (String) map.get("content");
        settingsService.saveQuestion(content);

        return ResponseEntity.ok(null);
    }
    
    
    /** 
     * @Function: 功能描述 通知设置
     * @Author: ChenXW
     * @Date: 18:18 2022/7/16
     */
    @PostMapping("/notifications/setting")
    public ResponseEntity notifications(@RequestBody Map map) {

        settingsService.saveSettings(map);

        return ResponseEntity.ok(null);
    }
    
    
    /** 
     * @Function: 功能描述 分页查询黑名单列表
     * @Author: ChenXW
     * @Date: 18:31 2022/7/16
     */
    @GetMapping("/blacklist")
    public ResponseEntity blacklist(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        //1、调用service查询
        PageResult pr = settingsService.blacklist(page,size);
        //2、构造返回
        return ResponseEntity.ok(pr);
    }

    /**
     * @Function: 功能描述 取消黑名单
     * @Author: ChenXW
     * @Date: 19:04 2022/7/16
     */
    @DeleteMapping("/blacklist/{uid}")
    public ResponseEntity deleteBlackList(@PathVariable("uid") Long blackUserId){
        settingsService.deleteBlackList(blackUserId);
        return ResponseEntity.ok(null);
    }

}
