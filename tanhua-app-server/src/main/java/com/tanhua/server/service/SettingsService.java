package com.tanhua.server.service;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.tanhua.dubbo.api.BlackListApi;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.SettingsApi;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.Settings;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.SettingsVo;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class SettingsService {

    @DubboReference
    private QuestionApi questionApi;

    @DubboReference
    private SettingsApi settingsApi;

    @DubboReference
    private BlackListApi blackListApi;

    //查询通用设置
    public SettingsVo settings() {
        SettingsVo vo = new SettingsVo();
        //获取用户id
        Long userId = UserHolder.getUserId();
        vo.setId(userId);
        //获取用户手机号码
        vo.setPhone(UserHolder.getMobile());
        //获取用户陌生人问题
        Question question = questionApi.findByUserId(userId);
        String txt = question == null ? "crud程序员?" : question.getTxt();
        vo.setStrangerQuestion(txt);
        //获取用户通知开关数据
        Settings settings = settingsApi.findByUserId(userId);
        if (settings != null) {
            vo.setGonggaoNotification(settings.getGonggaoNotification());
            vo.setPinglunNotification(settings.getPinglunNotification());
            vo.setLikeNotification(settings.getLikeNotification());
        }
        return vo;
    }


    //设置陌生人问题
    public void saveQuestion(String content) {
        //获取当前用户id
        Long userId = UserHolder.getUserId();
        //调用api查询当前用户的陌生人问题
        Question question = questionApi.findByUserId(userId);
        //判断问题是否存在
        if (question == null) {
            question = new Question();
            question.setUserId(userId);
            question.setTxt(content);
            questionApi.save(question);
        } else {
            question.setTxt(content);
            questionApi.update(question);
        }
    }

    //通知设置
    public void saveSettings(Map map) {
        boolean likeNotification = (Boolean) map.get("likeNotification");
        boolean pinglunNotification = (Boolean) map.get("pinglunNotification");
        boolean gonggaoNotification = (Boolean) map.get("gonggaoNotification");

        Long userId = UserHolder.getUserId();

        Settings settings = settingsApi.findByUserId(userId);

        if (settings == null) {
            settings = new Settings();
            settings.setUserId(userId);
            settings.setPinglunNotification(pinglunNotification);
            settings.setLikeNotification(likeNotification);
            settings.setGonggaoNotification(gonggaoNotification);
            settingsApi.save(settings);
        } else {
            settings.setPinglunNotification(pinglunNotification);
            settings.setLikeNotification(likeNotification);
            settings.setGonggaoNotification(gonggaoNotification);
            settingsApi.update(settings);
        }
    }


    //分页查询黑名单列表
    public PageResult blacklist(int page, int size) {
        //1、获取当前用户的id
        Long userId = UserHolder.getUserId();
        //2、调用API查询用户的黑名单分页列表  Ipage对象
        IPage<UserInfo> iPage = blackListApi.findByUserId(userId,page,size);
        //3、对象转化，将查询的Ipage对象的内容封装到PageResult中
        PageResult pr = new PageResult(page,size,iPage.getTotal(),iPage.getRecords());
        //4、返回
        return pr;
    }

    //取消黑名单
    public void deleteBlackList(Long blackUserId) {
        //获取当前用户id
        Long userId = UserHolder.getUserId();
        //调用api删除
        blackListApi.delete(userId,blackUserId);
    }
}
