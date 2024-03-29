package com.tanhua.server.service;


import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.HuanXinTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.FriendApi;
import com.tanhua.dubbo.api.UserApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.User;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Friend;
import com.tanhua.model.vo.ContactVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.UserInfoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
public class MessagesService {

    @DubboReference
    private UserApi userApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @DubboReference
    private FriendApi friendApi;

    @Autowired
    private HuanXinTemplate huanXinTemplate;

    /**
     * @Function: 功能描述 根据环信用户id查询用户详情
     * @Author: ChenXW
     * @Date: 9:59 2022/7/18
     */
    public UserInfoVo findUserInfoByHuanxin(String huanxinId) {
        //根据环信id查询用户
        User user = userApi.findByHuanxin(huanxinId);
        //根据用户id查询用户详情
        UserInfo userInfo = userInfoApi.findById(user.getId());
        UserInfoVo vo = new UserInfoVo();
        BeanUtils.copyProperties(userInfo, vo); //copy同名同类型的属性
        if (userInfo.getAge() != null) {
            vo.setAge(userInfo.getAge().toString());
        }
        return vo;
    }

    //添加好友关系
    public void contacts(Long friendId) {
        //将好友关系注册到环信
        Boolean aBoolean = huanXinTemplate.addContact(Constants.HX_USER_PREFIX + UserHolder.getUserId(),
                Constants.HX_USER_PREFIX + friendId);
        if (!aBoolean) {
            throw new BusinessException(ErrorResult.error());
        }
        //注册成功，记录好友关系到mongodb
        friendApi.save(UserHolder.getUserId(), friendId);
    }


    //分页查询联系人列表
    public PageResult findFriends(Integer page, Integer pagesize, String keyword) {
        List<Friend> list = friendApi.findByUserId(UserHolder.getUserId(), page, pagesize);
        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        List<Long> userIds = CollUtil.getFieldValues(list, "friendId", Long.class);

        //调用userInfoApi查询好友用户详情
        UserInfo info = new UserInfo();
        info.setNickname(keyword);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, info);

        List<ContactVo> vos=new ArrayList<>();
        for (Friend friend : list) {
            UserInfo userInfo = map.get(friend.getFriendId());
            if (userInfo!=null){
                ContactVo vo = ContactVo.init(userInfo);
                vos.add(vo);
            }
        }
        return new PageResult(page,pagesize,0l,vos);
    }
}
