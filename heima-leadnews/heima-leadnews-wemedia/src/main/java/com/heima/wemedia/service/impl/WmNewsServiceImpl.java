package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constans.message.NewsAutoScanConstants;
import com.heima.common.constans.wemedia.WemediaContans;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.utils.threadlocal.WmThreadLocalUtils;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @author: yp
 */
@Service
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Value("${fdfs.url}")
    private String fileServerUrl;

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    /**
     * 文章的提交
     * @param dto
     * @return
     */
    @Override
    public ResponseResult submitNews(WmNewsDto dto) {
        //1.参数的检查
        if(dto==null || StringUtils.isEmpty(dto.getContent())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.封装数据 userId, type(-1需要处理 null) createdTime,images
        WmUser user = WmThreadLocalUtils.getUser();
        if(user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.TOKEN_EXPIRE);
        }

        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews);
        wmNews.setUserId(user.getId());
        if(WemediaContans.WM_NEWS_AUTO_TYPE.equals(dto.getType())){
            wmNews.setType(null);
        }
        wmNews.setCreatedTime(new Date());
        List<String> imageList = dto.getImages();
        if (imageList != null && imageList.size() > 0) {
            wmNews.setImages(imageList.toString().replace("[", "").
                    replace("]", "").replace(fileServerUrl, "").replace(" ", ""));
        }

        //3.保存或者更新文章
        saveOrUpdateWmNews(wmNews);
        //4.保存文章图片和素材的关系
        List<Map> contentMapList = JSON.parseArray(wmNews.getContent(), Map.class);
        List<String> contentImageList = extractImageFromContent(contentMapList);
        if (WmNews.Status.SUBMIT.getCode() == wmNews.getStatus() && contentImageList.size() > 0) {
            //不是草稿
            saveRelativeInfoForContent(contentImageList, wmNews.getId());
        }

        //5.保存封面图片和素材的关系
        if (WmNews.Status.SUBMIT.getCode() == wmNews.getStatus()) {
            //不是草稿
            saveRelativeInfoForCover(imageList, wmNews,contentImageList);
        }
        return ResponseResult.okResult(null);
    }

    @Override
    public ResponseResult findList(WmNewsPageReqDto dto) {
        //1.参数检查
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数检查
        dto.checkParam();

        //2.分页条件查询
        IPage pageParam = new Page(dto.getPage(),dto.getSize());

        QueryWrapper<WmNews> queryWrapper = new QueryWrapper<>();

        //状态精确查询
        if(dto.getStatus() != null){
            queryWrapper.eq("status",dto.getStatus());
        }
        //频道精确查询
        if(null != dto.getChannelId()){
            queryWrapper.eq("channel_id",dto.getChannelId());
        }

        //时间范围查询
        if(dto.getBeginPubDate()!=null && dto.getEndPubDate()!=null){
            queryWrapper.between("publish_time",dto.getBeginPubDate(),dto.getEndPubDate());
        }

        //关键字模糊查询
        if(null != dto.getKeyword()){
            queryWrapper.like("title",dto.getKeyword());
        }

        //查询当前登录用户的信息
        WmUser user = WmThreadLocalUtils.getUser();
        if(user==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        queryWrapper.eq("user_id",user.getId());

        //按照创建日期倒序
        queryWrapper.orderByDesc("created_time");

        IPage pageResult = page(pageParam, queryWrapper);
        //3.结果封装返回
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)pageResult.getTotal());
        responseResult.setData(pageResult.getRecords());
        responseResult.setHost(fileServerUrl);
        return responseResult;
    }

    @Override
    public ResponseResult findWmNewsById(Integer id) {
        WmNews wmNews = getById(id);
        return ResponseResult.okResult(wmNews);
    }

    @Override
    public ResponseResult delNews(Integer id) {
        //1.参数判断
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "文章Id不可缺少");
        }
        //2.根据id查询
        WmNews wmNews = getById(id);
        if (wmNews == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章不存在");
        }

        //3.判断当前文章的状态
        //3.判断当前文章的状态  status==9  enable == 1
        if (wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode()) && wmNews.getEnable().equals(WemediaContans.WM_NEWS_ENABLE_UP)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "文章已发布，不能删除");
        }

        //4.去除文章和素材的关系
        QueryWrapper<WmNewsMaterial> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("news_id",id);
        wmNewsMaterialMapper.delete(queryWrapper);

        //5.删除文章
        removeById(id);
        return ResponseResult.okResult(null);
    }



    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        //1.参数检查
        if(dto==null || dto.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.根据ID查询
        WmNews wmNews = getById(dto.getId());
        if(wmNews==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"文章不存在");
        }
        //3.判断文章的状态
        if(WmNews.Status.PUBLISHED.getCode()!=wmNews.getStatus()){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"当前文章不是发布状态，不能上下架");
        }
        wmNews.setEnable(dto.getEnable());
        wmNews.setPublishTime(new Date());
        updateById(wmNews);

        //4.TODO 同步APP
        return ResponseResult.okResult(null);
    }

    /**
     * 保存封面图片和素材关系
     * @param imageList
     * @param wmNews
     * @param contentImageList
     */
    private void saveRelativeInfoForCover(List<String> imageList, WmNews wmNews, List<String> contentImageList) {
        if(wmNews.getType()==null){ //自动
            // 内容中没有图片，则为无图
            if(contentImageList.size()==0){
                wmNews.setType(WemediaContans.WM_NEWS_NONE_TYPE);
            }else if(contentImageList.size() > 0 && contentImageList.size() <= 2){
                //内容图片的个数小于等于2  则为单图截图一张图
                wmNews.setType(WemediaContans.WM_NEWS_SINGLE_TYPE);
                imageList = contentImageList.stream().limit(1).collect(Collectors.toList());
            }else if(contentImageList.size() > 2){
                //内容图片大于等于3，则为多图，截图三张图
                wmNews.setType(WemediaContans.WM_NEWS_MANY_TYPE);
                imageList = contentImageList.stream().limit(3).collect(Collectors.toList());
            }

            //修改文章信息
            if (imageList != null && imageList.size() > 0) {
                wmNews.setImages(imageList.toString().replace("[", "")
                        .replace("]", "").replace(fileServerUrl, "")
                        .replace(" ", ""));

            }
            updateById(wmNews);
        }

        if(imageList!=null && imageList.size()>0){
            saveRelative(imageList,wmNews.getId(),WemediaContans.WM_NEWS_COVER_REFERENCE);
        }
    }

    //保存文章图片和素材的关系
    private void saveRelativeInfoForContent(List<String> contentImageList, Integer wmNewsId) {
        saveRelative(contentImageList, wmNewsId, WemediaContans.WM_NEWS_CONTENT_REFERENCE);
    }

    private void saveRelative(List<String> contentImageList, Integer wmNewsId, Short type) {
        List<Integer> materialIds = new ArrayList<Integer>();

        //查询出素材ID
        LambdaQueryWrapper<WmMaterial> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WmMaterial::getUserId, WmThreadLocalUtils.getUser().getId());
        queryWrapper.in(WmMaterial::getUrl, contentImageList);
        List<WmMaterial> wmMaterialList = wmMaterialMapper.selectList(queryWrapper);
        if (wmMaterialList != null && wmMaterialList.size() > 0) {
            for (WmMaterial wmMaterial : wmMaterialList) {
                materialIds.add(wmMaterial.getId());
            }
        }
        //保存
        if (materialIds.size() > 0) {
            wmNewsMaterialMapper.saveRelations(materialIds, wmNewsId, type);
        }
    }

    //抽取image
    private List<String> extractImageFromContent(List<Map> contentMapList) {
        List<String> contentImageList = new ArrayList<String>();
        if(contentMapList!=null && contentMapList.size()>0){
            for (Map map : contentMapList) {
                if("image".equals(map.get("type"))){
                    contentImageList.add((String) map.get("value"));
                }
            }
        }
        return contentImageList;
    }

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;
    /**
     * 保存或者更新news
     * @param wmNews
     */
    private void saveOrUpdateWmNews(WmNews wmNews) {
        boolean flag;
        if (wmNews.getId() != null) {
            //解除WmNews和Material关系
            LambdaQueryWrapper<WmNewsMaterial> wrapper = new LambdaQueryWrapper<WmNewsMaterial>();
            wrapper.eq(WmNewsMaterial::getNewsId, wmNews.getId());
            wmNewsMaterialMapper.delete(wrapper);
            //更新WmNews
            flag= updateById(wmNews);
        } else {
            //新增WmNews
            flag = save(wmNews);
        }
        if(flag){
            kafkaTemplate.send(NewsAutoScanConstants.WM_NEWS_AUTO_SCAN_TOPIC,JSON.toJSONString(wmNews.getId()));
        }
    }
}