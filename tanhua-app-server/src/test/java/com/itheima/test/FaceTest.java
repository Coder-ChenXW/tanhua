package com.itheima.test;

import com.baidu.aip.face.AipFace;
import com.tanhua.autoconfig.template.AipFaceTemplate;
import com.tanhua.server.AppServerApplication;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = AppServerApplication.class)
public class FaceTest {

    @Autowired
    public AipFaceTemplate template;

    @Test
    public void detect(){
        boolean detect = template.detect("https://tanhua001.oss-cn-beijing.aliyuncs.com/2021/04/19/a3824a45-70e3-4655-8106-a1e1be009a5e.jpg");
        System.out.println(detect);
    }


    //设置APPID/AK/SK
    public static final String APP_ID = "23708310";
    public static final String API_KEY = "XiqaqHmB0wkLbgvPSjwSPM3O";
    public static final String SECRET_KEY = "qh6pguqsxfLo1G8GcKEorz2E5xp1ctRi";

    public static void main(String[] args) {
        // 初始化一个AipFace
        AipFace client = new AipFace(APP_ID, API_KEY, SECRET_KEY);

        // 可选：设置网络连接参数
        client.setConnectionTimeoutInMillis(2000);
        client.setSocketTimeoutInMillis(60000);


        // 调用接口
        String image = "https://tanhua001.oss-cn-beijing.aliyuncs.com/2021/04/19/a3824a45-70e3-4655-8106-a1e1be009a5e.jpg";
        String imageType = "URL";

        HashMap<String, String> options = new HashMap<String, String>();
        options.put("face_field", "age");
        options.put("max_face_num", "2");
        options.put("face_type", "LIVE");
        options.put("liveness_control", "LOW");

        // 人脸检测
        JSONObject res = client.detect(image, imageType, options);
        System.out.println(res.toString(2));

    }

}
