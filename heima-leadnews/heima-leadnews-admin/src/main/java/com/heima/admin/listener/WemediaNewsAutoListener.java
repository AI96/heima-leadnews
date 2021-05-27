package com.heima.admin.listener;

import com.heima.admin.service.WemediaNewsAutoScanService;
import com.heima.common.constans.message.NewsAutoScanConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @Description:
 * @author: yp
 */
@Component
@Slf4j
public class WemediaNewsAutoListener {

    @Autowired
    private WemediaNewsAutoScanService wemediaNewsAutoScanService;

    @KafkaListener(topics = {NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_TOPIC})
    public void recevieMessage(String wmNewsId){
        if(StringUtils.isNotEmpty(wmNewsId)){
            log.info("wmNewsId={}",wmNewsId);
            wemediaNewsAutoScanService.autoScanByMediaNewsId(Integer.parseInt(wmNewsId));
        }
    }
}
