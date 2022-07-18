package com.tanhua.dubbo.api;

import com.tanhua.model.mongo.Video;

import java.util.List;

public interface VideoApi {

    //保存视频
    String save(Video video);

    //根据vid查询数据列表
    List<Video> findMovementsByVids(List<Long> vids);

    //分页查询数据列表
    List<Video> queryVideoList(int page, Integer pagesize);
}
