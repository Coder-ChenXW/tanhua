package com.tanhua.server.service;

import cn.hutool.core.collection.CollUtil;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.CommentApi;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.enums.CommentType;
import com.tanhua.model.mongo.Comment;
import com.tanhua.model.vo.CommentVo;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Service
@Slf4j
public class CommentsService {

    @DubboReference
    public CommentApi commentApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    //发布评论
    public void saveComments(String movementId, String comment) {
        //获取操作用户id
        Long userId = UserHolder.getUserId();
        //构造Comment
        Comment comment1 = new Comment();
        comment1.setPublishId(new ObjectId(movementId));
        comment1.setCommentType(CommentType.COMMENT.getType());
        comment1.setContent(comment);
        comment1.setUserId(userId);
        comment1.setCreated(System.currentTimeMillis());
        //调用api保存
        Integer commentCount = commentApi.save(comment1);
        log.info("commentCount:" + commentCount);
    }


    //分页查询评论列表
    public PageResult findComments(String movementId, Integer page, Integer pagesize) {
        //1、调用API查询评论列表
        List<Comment> list = commentApi.findComments(movementId, CommentType.COMMENT, page, pagesize);
        //2、判断list集合是否存在
        if (CollUtil.isEmpty(list)) {
            return new PageResult();
        }
        //3、提取所有的用户id,调用UserInfoAPI查询用户详情
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        //4、构造vo对象
        List<CommentVo> vos = new ArrayList<>();
        for (Comment comment : list) {
            UserInfo userInfo = map.get(comment.getUserId());
            if (userInfo != null) {
                CommentVo vo = CommentVo.init(userInfo, comment);
                vos.add(vo);
            }
        }
        //5、构造返回值
        return new PageResult(page, pagesize, 0l, vos);
    }


    /**
     * @Function: 功能描述 动态点赞
     * @Author: ChenXW
     * @Date: 20:39 2022/7/17
     */
    public Integer likeComment(String movementId) {
        //调用api查询用户是否点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LIKE);

        //已点赞抛出异常
        if (hasComment) {
            throw new BusinessException(ErrorResult.likeError());
        }
        //未点赞调用api数据保存到mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.save(comment);
        //拼接redis的key
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().put(key, hashKey, "1");
        return count;
    }

    
    /** 
     * @Function: 功能描述 取消点赞
     * @Author: ChenXW
     * @Date: 21:05 2022/7/17
     */
    public Integer dislikeComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LIKE);
        //2、如果未点赞，抛出异常
        if(!hasComment) {
            throw new BusinessException(ErrorResult.disLikeError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LIKE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LIKE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }


    /**
     * @Function: 功能描述 喜欢
     * @Author: ChenXW
     * @Date: 21:43 2022/7/17
     */
    public Integer loveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LOVE);
        //2、如果已经喜欢，抛出异常
        if(hasComment) {
            throw  new BusinessException(ErrorResult.loveError());
        }
        //3、调用API保存数据到Mongodb
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        comment.setCreated(System.currentTimeMillis());
        Integer count = commentApi.save(comment);
        //4、拼接redis的key，将用户的点赞状态存入redis
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().put(key,hashKey,"1");
        return count;
    }

    /**
     * @Function: 功能描述 取消喜欢
     * @Author: ChenXW
     * @Date: 21:45 2022/7/17
     */
    public Integer disloveComment(String movementId) {
        //1、调用API查询用户是否已点赞
        Boolean hasComment = commentApi.hasComment(movementId,UserHolder.getUserId(),CommentType.LOVE);
        //2、如果未点赞，抛出异常
        if(!hasComment) {
            throw new BusinessException(ErrorResult.disloveError());
        }
        //3、调用API，删除数据，返回点赞数量
        Comment comment = new Comment();
        comment.setPublishId(new ObjectId(movementId));
        comment.setCommentType(CommentType.LOVE.getType());
        comment.setUserId(UserHolder.getUserId());
        Integer count = commentApi.delete(comment);
        //4、拼接redis的key，删除点赞状态
        String key = Constants.MOVEMENTS_INTERACT_KEY + movementId;
        String hashKey = Constants.MOVEMENT_LOVE_HASHKEY + UserHolder.getUserId();
        redisTemplate.opsForHash().delete(key,hashKey);
        return count;
    }
}
