package com.heima.apis.wemedia;

import com.heima.model.admin.dtos.NewsAuthDto;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.vo.WmNewsVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 自媒体文章接口
 */
public interface WmNewsControllerApi {

    /**
     * 文章上下架
     * @param dto
     * @return
     */
    ResponseResult downOrUp(WmNewsDto dto);

    /**
     * 提交文章
     * @param dto
     * @return
     */
    ResponseResult submitNews(WmNewsDto dto);

    /**
     * 分页带条件查询自媒体文章列表
     * @param wmNewsPageReqDto
     * @return
     */
    public ResponseResult findList(WmNewsPageReqDto wmNewsPageReqDto);


    /**
     * 根据ID查询文章
     * @param id
     * @return
     */
    public ResponseResult findWmNewsById(Integer id);

    /**
     * 删除文章
     * @param id
     * @return
     */
    ResponseResult delNews(Integer id);


    /**
     * 根据ID查询文章
     * @param id
     * @return
     */
    public WmNews findById(Integer id);

    /**
     * 更新WmNews
     * @param wmNews
     * @return
     */
     ResponseResult updateWmNews(WmNews wmNews);


    /**
     * 查询需要发布的WmNews
     * @return
     */
     List<Integer> findRelease();


    PageResponseResult findNewsList(NewsAuthDto dto);


    WmNewsVo findWmNewsVo(Integer id);


}