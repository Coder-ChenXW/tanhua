package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.RecommendUser;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface RecommendUserApi {

    RecommendUser queryWithMaxScore(Long toUserId);

    //分页查询
    PageResult queryRecommendUserList(Integer page, Integer pagesize, Long toUserId);

    RecommendUser queryByUserId(Long userId, Long userId1);

    //探花查询推荐用户列表
    List<RecommendUser> queryCardsList(Long userId, int i);
}
