package com.heima.apis.admin;

import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.ResponseResult;

public interface NewsAuthControllerApi {
    /**
     * 查询自媒体文章列表
     * @param dto
     * @return
     */
    ResponseResult findNews(NewsAuthDto dto);

    /**
     * 查询WmNews详情
     * @param id
     * @return
     */
    ResponseResult findOne(Integer id);


    /**
     * 审核通过
     * @param dto
     * @return
     */
    ResponseResult authPass(NewsAuthDto dto);

    /**
     * 驳回
     * @param dto
     * @return
     */
    ResponseResult authFail(NewsAuthDto dto);
}