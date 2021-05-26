package com.heima;

import com.heima.admin.AdminApplication;
import com.heima.admin.mapper.AdSensitiveMapper;
import com.heima.common.aliyun.GreeTextScan;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.fastdfs.FastDFSClient;
import com.heima.model.admin.pojos.AdSensitive;
import com.heima.utils.common.SensitiveWordUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Description:
 * @author: yp
 */
@SpringBootTest(classes = AdminApplication.class)
@RunWith(SpringRunner.class)
public class SensitiveTest {


    @Autowired
    private AdSensitiveMapper adSensitiveMapper;
    @Test
    //文本检测
    public void fun01() throws Exception {
        List<String> list = adSensitiveMapper.findAllSensitive();
        SensitiveWordUtil.initMap(list);
        Map<String, Integer> map = SensitiveWordUtil.matchWords("法轮功");
        System.out.println(map);
    }
}
