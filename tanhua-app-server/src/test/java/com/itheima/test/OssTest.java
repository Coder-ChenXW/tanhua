package com.itheima.test;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.tanhua.autoconfig.template.OssTemplate;
import com.tanhua.server.AppServerApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class OssTest {

    @Autowired
    private OssTemplate template;

    @Test
    public void testTemplateUpload() throws FileNotFoundException {
        String path = "D:\\project-img\\reggie\\zhoujielun.png";

        FileInputStream inputStream = new FileInputStream(new File(path));
        String imageUrl = template.upload(path, inputStream);
        System.out.println(imageUrl);
    }


    /**
     * @Function: 功能描述 将1.jpg上传到阿里云
     * @Author: ChenXW
     * @Date: 11:29 2022/7/16
     */
    @Test
    public void testOss() throws FileNotFoundException {

        //配置图片路径
        String path = "D:\\project-img\\reggie\\1.jpg";

        FileInputStream inputStream = new FileInputStream(new File(path));

        String filename = new SimpleDateFormat("yyyy/MM/dd").format(new Date())
                + "/" + UUID.randomUUID().toString() + path.substring(path.lastIndexOf("."));

        // yourEndpoint填写Bucket所在地域对应的Endpoint。以华东1（杭州）为例，Endpoint填写为https://oss-cn-hangzhou.aliyuncs.com。
        String endpoint = "oss-cn-beijing.aliyuncs.com";
        // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
        String accessKeyId = "LTAI5tCCK6DfMXZFPZxZB6qN";
        String accessKeySecret = "ZgBBhd015DUNAcMgCmM5ILHlkMvyR9";

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        // 填写Byte数组
        // 填写Bucket名称和Object完整路径。Object完整路径中不能包含Bucket名称。
        ossClient.putObject("tanhuacouldplatform", filename, inputStream);


        // 关闭OSSClient。
        ossClient.shutdown();
    }
}
