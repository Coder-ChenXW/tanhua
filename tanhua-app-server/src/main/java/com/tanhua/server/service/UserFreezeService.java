package com.tanhua.server.service;


import com.alibaba.fastjson.JSON;
import com.tanhua.commons.utils.Constants;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.server.exception.BusinessException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class UserFreezeService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * @Function: 功能描述 判断用户是否被冻结，如果被冻结抛出异常
     * @Author: ChenXW
     * @Date: 21:09 2022/7/20
     */
    public void checkUserStatus(String state, Long userId) {
        //拼接key从redis中查询数据
        String key = Constants.USER_FREEZE + userId;
        String value = redisTemplate.opsForValue().get(key);
        //如果数据存在，且冻结范围一致，抛出异常
        if (!StringUtils.isEmpty(value)){
            Map map = JSON.parseObject(value, Map.class);
            String freezingRange = (String) map.get("freezingRange");
            if (state.equals(freezingRange)){
                throw new BusinessException(ErrorResult.builder().errMessage("用户被冻结").build());
            }
        }
    }

}
