package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.fastjson.JSON;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.QuestionApi;
import com.tanhua.dubbo.api.RecommendUserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.Question;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.dto.RecommendUserDto;
import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.TodayBest;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TanhuaService {

    @DubboReference
    private RecommendUserApi recommendUserApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private QuestionApi questionApi;

    @Autowired
    private HuanXinTemplate template;

    //查询今日佳人数据
    public TodayBest todayBest() {
        //1、获取用户id
        Long userId = UserHolder.getUserId();
        //2、调用API查询
        RecommendUser recommendUser = recommendUserApi.queryWithMaxScore(userId);
        if (recommendUser == null) {
            recommendUser = new RecommendUser();
            recommendUser.setUserId(1l);
            recommendUser.setScore(99d);
        }
        //3、将RecommendUser转化为TodayBest对象
        UserInfo userInfo = userInfoApi.findById(recommendUser.getUserId());
        TodayBest vo = TodayBest.init(userInfo, recommendUser);
        //4、返回
        return vo;
    }

//    //查询分页推荐好友列表
//    public PageResult recommendation(RecommendUserDto dto) {
//        //获取用户id
//        Long userId = UserHolder.getUserId();
//        //调用recommendUserApi分页查询数据列表（PageResult -- RecommendUser）
//        PageResult pr = recommendUserApi.queryRecommendUserList(dto.getPage(),dto.getPagesize(),userId);
//        //获取分页中的RecommendUser数据列表
//        List<RecommendUser> items = (List<RecommendUser>) pr.getItems();
//        //判断列表是否为空
//        if(items == null) {
//            return pr;
//        }
//        //循环RecommendUser数据列表，根据推荐的用户id查询用户详情
//        List<TodayBest> list = new ArrayList<>();
//        for (RecommendUser item : items) {
//            Long recommendUserId = item.getUserId();
//            UserInfo userInfo = userInfoApi.findById(recommendUserId);
//            if(userInfo != null) {
//                //条件判断
//                if(!StringUtils.isEmpty(dto.getGender()) && !dto.getGender().equals(userInfo.getGender())) {
//                    continue;
//                }
//                if(dto.getAge() != null && dto.getAge() < userInfo.getAge()) {
//                    continue;
//                }
//                TodayBest vo = TodayBest.init(userInfo, item);
//                list.add(vo);
//            }
//        }
//        //构造返回值
//        pr.setItems(list);
//        return pr;
//    }

    //查询分页推荐好友列表
    public PageResult recommendation(RecommendUserDto dto) {
        //获取用户id
        Long userId = UserHolder.getUserId();
        //调用recommendUserApi分页查询数据列表（PageResult -- RecommendUser）
        PageResult pr = recommendUserApi.queryRecommendUserList(dto.getPage(), dto.getPagesize(), userId);
        //获取分页中的RecommendUser数据列表
        List<RecommendUser> items = (List<RecommendUser>) pr.getItems();
        //判断列表是否为空
        if (items == null) {
            return pr;
        }
        //提取所有推荐的用户id列表
        List<Long> ids = CollUtil.getFieldValues(items, "userId", Long.class);
        UserInfo userInfo = new UserInfo();
        userInfo.setAge(dto.getAge());
        userInfo.setGender(dto.getGender());
        //构建查询条件，批量查询所有的用户详情
        Map<Long, UserInfo> map = userInfoApi.findByIds(ids, userInfo);
        //循环推荐的数据列表，构建vo对象
        List<TodayBest> list = new ArrayList<>();
        for (RecommendUser item : items) {
            UserInfo info = map.get(item.getUserId());
            if (info != null) {
                TodayBest vo = TodayBest.init(info, item);
                list.add(vo);
            }
        }
        //构造返回值
        pr.setItems(list);
        return pr;
    }


    //查看佳人详情
    public TodayBest personalInfo(Long userId) {
        //根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(userId);
        //根据操作人id和查看的用户id查询两者的推荐数据
        RecommendUser user = recommendUserApi.queryByUserId(userId, UserHolder.getUserId());
        //构造返回值
        return TodayBest.init(userInfo, user);
    }

    //查看陌生人问题
    public String strangerQuestions(Long userId) {
        Question question = questionApi.findByUserId(userId);
        return question == null ? "CRUD程序员?" : question.getTxt();
    }

    //回复陌生人问题
    public void replyQuestions(Long userId, String reply) {
        Long currentUserId = UserHolder.getUserId();
        UserInfo userInfo = userInfoApi.findById(currentUserId);
        Map map = new HashMap();
        map.put("userId", currentUserId);
        map.put("huanXinId", Constants.HX_USER_PREFIX + currentUserId);
        map.put("nickname", userInfo.getNickname());
        map.put("strangerQuestion", strangerQuestions(userId));
        map.put("reply", reply);
        String message = JSON.toJSONString(map);

        //调用template发送消息
        Boolean aBoolean = template.sendMsg(Constants.HX_USER_PREFIX + userId, message);
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }

    }
}
