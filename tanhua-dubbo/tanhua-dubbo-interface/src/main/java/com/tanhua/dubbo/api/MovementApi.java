package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Movement;
import com.tanhua.model.vo.PageResult;

import java.util.List;

public interface MovementApi {

    //发布动态
    void publish(Movement movement);


    //根据用户id查询用户动态
    PageResult findByUserId(Long userId, Integer page, Integer pagesize);

    //根据用户id查询用户好友发送的动态列表
    List<Movement> findFriendMovements(Integer page, Integer pagesize, Long userId);

    //随机获取多条数据
    List<Movement> randomMovements(Integer pagesize);

    //根据pid查询动态数据
    List<Movement> findMovementsByPids(List<Long> pids);

    Movement findById(String movementId);

}
