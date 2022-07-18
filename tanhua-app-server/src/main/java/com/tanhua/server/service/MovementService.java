package com.tanhua.server.service;


import cn.hutool.core.collection.CollUtil;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.MovementApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VisitorsApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Movement;
import com.tanhua.model.mongo.Visitors;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.MovementsVo;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VisitorsVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MovementService {
    @Autowired
    private OssTemplate ossTemplate;

    @DubboReference
    private MovementApi movementApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * @Function: 功能描述 发布动态
     * @Author: ChenXW
     * @Date: 17:00 2022/7/17
     */
    public void publishMovement(Movement movement, MultipartFile[] imageContent) throws IOException {
        //判断发布动态的内容是否存在
        if (StringUtils.isEmpty(movement.getTextContent())) {
            throw new BusinessException(ErrorResult.contentError());
        }
        //获取当前登录的用户id
        Long userId = UserHolder.getUserId();

        List<String> medias = new ArrayList<>();
        for (MultipartFile multipartFile : imageContent) {
            String upload = ossTemplate.upload(multipartFile.getOriginalFilename(), multipartFile.getInputStream());
            medias.add(upload);
        }

        //将数据封装到movement对象
        movement.setUserId(userId);
        movement.setMedias(medias);
        //调用api完成发布
        movementApi.publish(movement);
    }

    /**
     * @Function: 功能描述 查询个人动态
     * @Author: ChenXW
     * @Date: 17:37 2022/7/17
     */
    public PageResult findByUserId(Long userId, Integer page, Integer pagesize) {
        //根据用户id调用api查询个人内容
        PageResult pr = movementApi.findByUserId(userId, page, pagesize);
        //获取PageResult中的item对象
        List<Movement> items = (List<Movement>) pr.getItems();
        //非空判断
        if (items == null) {
            return pr;
        }
        UserInfo userInfo = userInfoApi.findById(userId);
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement item : items) {
            MovementsVo vo = MovementsVo.init(userInfo, item);
            vos.add(vo);
        }
        //构建返回值
        pr.setItems(vos);
        return pr;
    }


    /**
     * @Function: 功能描述 查询好友动态
     * @Author: ChenXW
     * @Date: 18:03 2022/7/17
     */
    public PageResult findFriendMovements(Integer page, Integer pagesize) {
        //获取当前用户id
        Long userId = UserHolder.getUserId();
        //调用api查询当前用户好友发布的动态列表
        List<Movement> list = movementApi.findFriendMovements(page, pagesize, userId);
        return getPageResult(page, pagesize, list);
    }

    private PageResult getPageResult(Integer page, Integer pagesize, List<Movement> list) {
        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        //提取动态发布人的id列表
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        //根据用户id列表获取用户详情
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        //一个movement构造一个vo对象
        List<MovementsVo> vos = new ArrayList<>();
        for (Movement movement : list) {
            UserInfo userInfo = map.get(movement.getUserId());
            if (userInfo != null) {
                MovementsVo vo = MovementsVo.init(userInfo, movement);
                //修复点赞状态bug.判断hashKey是否存在
                String key = Constants.MOVEMENTS_INTERACT_KEY + movement.getId().toHexString();
                String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
                if (redisTemplate.opsForHash().hasKey(key, hashKey)) {
                    vo.setHasLiked(1);
                }
                vos.add(vo);
            }
        }
        //构造pageResult并返回
        return new PageResult(page, pagesize, 0l, vos);
    }


    /**
     * @Function: 功能描述 查询推荐动态
     * @Author: ChenXW
     * @Date: 18:31 2022/7/17
     */
    public PageResult findRecommendMovements(Integer page, Integer pagesize) {
        //从redis中获取推荐数据
        String redisKey = Constants.MOVEMENTS_RECOMMEND + UserHolder.getUserId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        //判断推荐数据是否存在
        List<Movement> list = Collections.EMPTY_LIST;
        if (StringUtils.isEmpty(redisValue)) {
            //不存在随机构造10条数据
            list = movementApi.randomMovements(pagesize);
        } else {
            //存在处理pid
            String[] values = redisValue.split(",");
            //判断当前页起始数是否小于数组总数
            if ((page - 1) * pagesize < values.length) {
                List<Long> pids = Arrays.stream(values).skip((page - 1) * pagesize).limit(pagesize)
                        .map(e -> Long.valueOf(e))
                        .collect(Collectors.toList());
                list = movementApi.findMovementsByPids(pids);
            }
        }
        //调用公共方法构造返回值
        return getPageResult(page, pagesize, list);
    }


    //根据id查询
    public MovementsVo findById(String movementId) {
        //调用api根据id查询动态详细
        Movement movement = movementApi.findById(movementId);
        //转换vo对象
        if (movement != null) {
            UserInfo userInfo = userInfoApi.findById(movement.getUserId());
            return MovementsVo.init(userInfo, movement);
        } else {
            return null;
        }
    }

    @DubboReference
    private VisitorsApi visitorsApi;

    //首页访客列表
    public List<VisitorsVo> queryVisitorsList() {
        //查询访客时间
        String key = Constants.VISITORS_USER;
        String hashKey = String.valueOf(UserHolder.getUserId());
        String value = (String) redisTemplate.opsForHash().get(key, hashKey);
        Long date = StringUtils.isEmpty(value) ? null : Long.valueOf(value);
        //调用api查询数据列表
        List<Visitors> list = visitorsApi.queryMyVisitors(date, UserHolder.getUserId());
        if (CollUtil.isEmpty(list)){
            return new ArrayList<>();
        }
        //提取用户id
        List<Long> userIds = CollUtil.getFieldValues(list, "visitorUserId", Long.class);
        //查看用户详情
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        //构造返回
        List<VisitorsVo> vos = new ArrayList<>();

        for (Visitors visitors : list) {
            UserInfo userInfo = map.get(visitors.getVisitorUserId());
            if(userInfo != null) {
                VisitorsVo vo = VisitorsVo.init(userInfo, visitors);
                vos.add(vo);
            }
        }
        return vos;
    }
}
