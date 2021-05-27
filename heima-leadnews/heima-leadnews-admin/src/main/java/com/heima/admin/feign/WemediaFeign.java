package com.heima.admin.feign;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmNews;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @author: yp
 */
@FeignClient("leadnews-wemedia")
public interface WemediaFeign {

    @GetMapping("/api/v1/news/one/{id}")
    ResponseResult findWmNewsById(@PathVariable(value = "id") Integer id);


    @PostMapping("/api/v1/news/update")
    public ResponseResult updateWmNews(@RequestBody WmNews wmNews);
}
