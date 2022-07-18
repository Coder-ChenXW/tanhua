package com.tanhua.server.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.PageUtil;
import com.github.tobato.fastdfs.domain.conn.FdfsWebServer;
import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.commons.utils.Constants;
import com.tanhua.dubbo.api.UserInfoApi;
import com.tanhua.dubbo.api.VideoApi;
import com.tanhua.model.domain.UserInfo;
import com.tanhua.model.mongo.Video;
import com.tanhua.model.vo.ErrorResult;
import com.tanhua.model.vo.PageResult;
import com.tanhua.model.vo.VideoVo;
import com.tanhua.server.exception.BusinessException;
import com.tanhua.server.interceptor.UserHolder;
import org.apache.commons.lang.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class SmallVideosService {

    @Autowired
    private FastFileStorageClient client;

    @Autowired
    private FdfsWebServer webServer;

    @Autowired
    private OssTemplate ossTemplate;

    @DubboReference
    private VideoApi videoApi;

    @DubboReference
    private UserInfoApi userInfoApi;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * @Function: 功能描述 上传视频
     * @Author: ChenXW
     * @Date: 19:28 2022/7/18
     */
    public void saveVideos(MultipartFile videoThumbnail, MultipartFile videoFile) throws IOException {

        if (videoFile.isEmpty() || videoThumbnail.isEmpty()) {
            throw new BusinessException(ErrorResult.error());
        }
        //将视频上传到FastDFS
        String filename = videoFile.getOriginalFilename();
        filename = filename.substring(filename.lastIndexOf(".") + 1);
        StorePath storePath = client.uploadFile(videoFile.getInputStream(), videoFile.getSize(), filename, null);
        String videoUrl = webServer.getWebServerUrl() + storePath.getFullPath();
        //将封面上传到阿里云oss
        String imageUrl = ossTemplate.upload(videoThumbnail.getOriginalFilename(), videoThumbnail.getInputStream());
        //构建videos对象
        Video video = new Video();
        video.setUserId(UserHolder.getUserId());
        video.setPicUrl(imageUrl);
        video.setVideoUrl(videoUrl);
        video.setText("薰悟空");
        //调用api保存数据
        String videoId = videoApi.save(video);
        if (StringUtils.isEmpty(videoId)) {
            throw new BusinessException(ErrorResult.error());
        }
    }


    //查询视频列表
    public PageResult queryVideoList(Integer page, Integer pagesize) {
        //查询redis数据
        String redisKey = Constants.VIDEOS_RECOMMEND + UserHolder.getUserId();
        String redisValue = redisTemplate.opsForValue().get(redisKey);
        //判断redis数据是否存在,是否满足分页条数
        List<Video> list = new ArrayList<>();
        int redisPages = 0;
        if (StringUtils.isEmpty(redisValue)) {
            //如果数据存在，根据VID查询数据
            String[] values = redisValue.split(",");
            //判断当前页的起始条数是否小于数组总数
            if ((page - 1) * pagesize < values.length) {
                List<Long> vids = Arrays.stream(values).skip((page - 1) * pagesize).limit(pagesize)
                        .map(e -> Long.valueOf(e))
                        .collect(Collectors.toList());
                //调用API根据PID数组查询动态数据
                list = videoApi.findMovementsByVids(vids);
            }
            redisPages = PageUtil.totalPage(values.length, pagesize);
        }
        //如果数据不存在，分页查询视频数据

        if (list.isEmpty()) {
            list = videoApi.queryVideoList(page - redisPages, pagesize);
        }
        //提取视频列表中所有的用户id
        List<Long> userIds = CollUtil.getFieldValues(list, "userId", Long.class);
        //查询用户信息
        Map<Long, UserInfo> map = userInfoApi.findByIds(userIds, null);
        //构建返回值
        List<VideoVo> vos = new ArrayList<>();
        for (Video video : list) {
            UserInfo info = map.get(video.getUserId());
            if (info != null) {
                VideoVo vo = VideoVo.init(info, video);
                vos.add(vo);
            }
        }
        return new PageResult(page, pagesize, 0l, vos);
    }
}
