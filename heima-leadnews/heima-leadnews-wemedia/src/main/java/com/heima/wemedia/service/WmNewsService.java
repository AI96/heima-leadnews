package com.heima.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {
    ResponseResult findList(WmNewsPageReqDto wmNewsPageReqDto);

    ResponseResult findWmNewsById(Integer id);

    ResponseResult delNews(Integer id);

    /**
     * 文章的提交
     * @param dto
     * @return
     */
    ResponseResult submitNews(WmNewsDto dto);

    ResponseResult downOrUp(WmNewsDto dto);
}