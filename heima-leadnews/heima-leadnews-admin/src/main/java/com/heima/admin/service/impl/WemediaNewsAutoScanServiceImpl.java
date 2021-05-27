package com.heima.admin.service.impl;

import com.alibaba.fastjson.JSON;
import com.heima.admin.feign.ArticleFeign;
import com.heima.admin.feign.WemediaFeign;
import com.heima.admin.mapper.AdChannelMapper;
import com.heima.admin.mapper.AdSensitiveMapper;
import com.heima.admin.service.WemediaNewsAutoScanService;
import com.heima.common.aliyun.GreeTextScan;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.fastdfs.FastDFSClient;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.utils.common.SensitiveWordUtil;
import com.mongodb.bulk.IndexRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.ldap.HasControls;
import javax.net.ssl.SNIHostName;
import java.io.IOException;
import java.util.*;

/**
 * @Description:
 * @author: yp
 */
@Service
@Slf4j
public class WemediaNewsAutoScanServiceImpl implements WemediaNewsAutoScanService {

    @Autowired
    private ArticleFeign articleFeign;

    @Autowired
    private WemediaFeign wemediaFeign;

    @Autowired
    private GreenImageScan greenImageScan;

    @Autowired
    private GreeTextScan greeTextScan;

    @Autowired
    private FastDFSClient fastDFSClient;

    @Value("${fdfs.url}")
    private String fileServerUrl;

    @Autowired
    private AdSensitiveMapper adSensitiveMapper;

    @Autowired
    private AdChannelMapper adChannelMapper;

    @Override
    public void autoScanByMediaNewsId(Integer id) {
        //1. 当自媒体用户提交发布文章之后，会发消息给kafka提交审核，平台运营端接收文章信息(在heima-leadnews-wemedia实现了)
        //2. 根据自媒体文章id查询文章信息
        ResponseResult responseResult = wemediaFeign.findWmNewsById(id);
        Object data = responseResult.getData();
        if (data == null) {
            return;
        }
        WmNews wmNews = JSON.parseObject(JSON.toJSONString(data), WmNews.class);
        if (wmNews == null) {
            return;
        }
        //3. 如果当前文章的状态为4（人工审核通过），则无需再进行自动审核审核，保存app文章相关数据即可
        if (wmNews.getStatus() == 4) {
            saveAppArticle(wmNews);
            return;
        }
        //4. 文章状态为8,发布时间<当前时间,则直接保存app文章相关数据
        if (wmNews.getStatus() == 8 && wmNews.getPublishTime().getTime() < System.currentTimeMillis()) {
            saveAppArticle(wmNews);
            return;
        }

        //5. 文章状态为1，则进行自动审核
        if (wmNews.getStatus() == 1) {
            Map<String, Object> contentMap = handleTextAndImages(wmNews);
            //5.1 调用阿里云文本反垃圾服务，进行文本审核，如果审核不成功或需要人工审核，修改自媒体文章状态
            boolean textScanBoolean = handleTextScan((String) contentMap.get("text"), wmNews);
            if (!textScanBoolean) {
                return;
            }
            //5.2 调用阿里云图片审核服务，如果审核不通过或需要人工审核，修改自媒体文章状态
            boolean imageScanBoolean = handleImageScan((List<String>)contentMap.get("images"), wmNews);
            if (!imageScanBoolean) {
                return;
            }

            //5.3 文章内容中是否有自管理的敏感词，如果有则审核不通过，修改自媒体文章状态
            boolean sensitiveScanBoolean = handleSensitiveScan(((String) contentMap.get("text")), wmNews);
            if (!sensitiveScanBoolean) {
                return;
            }


            //5.4 自媒体文章发布时间大于当前时间，修改自媒体文章状态为8（审核通过待发布状态）
            if (wmNews.getPublishTime().getTime() > System.currentTimeMillis()) {
                wmNews.setStatus((short) 8);
                wmNews.setReason("审核通过，待发布");
                updateWmNews(wmNews);
                return;
            }
            //5.5 审核通过，修改自媒体文章状态为 9 （审核通过）
            saveAppArticle(wmNews);
        }

    }

    private boolean handleSensitiveScan(String text, WmNews wmNews) {
        boolean flag = true;
        List<String> sensitiveWords = adSensitiveMapper.findAllSensitive();
        SensitiveWordUtil.initMap(sensitiveWords);
        Map<String, Integer> map = SensitiveWordUtil.matchWords(text);
        if (map.size() > 0) {
            log.error("敏感词过滤没有通过，包含了敏感词:{}", map);
            //找到了敏感词，审核不通过
            wmNews.setStatus((short) 2);
            wmNews.setReason("文章内容中有敏感词汇");
            updateWmNews(wmNews);
            flag = false;
        }

        return flag;
    }

    //文本审核
    private boolean handleTextScan(String text, WmNews wmNews) {
        boolean flag = true;
        try {
            Map map = greeTextScan.greeTextScan(text);
            if (!"pass".equals(map.get("suggestion"))) {
                if ("block".equals(map.get("suggestion"))) {
                    //审核不通过
                    wmNews.setStatus((short) 2);
                    wmNews.setReason("文章内容中有敏感词汇");
                    updateWmNews(wmNews);
                    flag = false;
                }
                if ("review".equals(map.get("suggestion"))) {
                    //文本需要进一步人工审核
                    wmNews.setStatus((short) 3);
                    wmNews.setReason("文章内容中有不确定词汇");
                    updateWmNews(wmNews);
                    flag = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }
        return flag;
    }



    //图片审核
    private boolean handleImageScan(List<String> images, WmNews wmNews) {
        boolean flag = true;
        try {
            List<byte[]> imageList = new ArrayList<byte[]>();
            for (String image : images) {
                if (image.contains(fileServerUrl)) {
                    image = image.replace(fileServerUrl, "");
                }
                //group1/M00/00/00/wKjTiGB9Rv2AKPr2AAAZOimJI7M665.png,group1/M00/00/00/wKjTiGB9RuKAUa8WAADf8Lbu0bY539.png,group1/M00/00/00/wKjTiGB57kKAAHEcAAC88bW6B7o605.png
                int index = image.indexOf("/");
                String groupName = image.substring(0, index);
                String imagePath = image.substring(index + 1);
                imageList.add(fastDFSClient.download(groupName, imagePath));
            }

            Map map = greenImageScan.imageScan(imageList);
            if (!"pass".equals(map.get("suggestion"))) {
                if ("block".equals(map.get("suggestion"))) {
                    //审核不通过
                    wmNews.setStatus((short) 2);
                    wmNews.setReason("图片中含有敏感内容");
                    updateWmNews(wmNews);
                    flag = false;
                }
                if ("review".equals(map.get("suggestion"))) {
                    //文本需要进一步人工审核
                    wmNews.setStatus((short) 3);
                    wmNews.setReason("图片中含有不确定内容");
                    updateWmNews(wmNews);
                    flag = false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            flag = false;
        }

        return flag;
    }

    //抽取成Map ("text",sb), ("images",List)
    private Map<String, Object> handleTextAndImages(WmNews wmNews) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        StringBuilder sb = new StringBuilder();
        List<String> imgList = new ArrayList<String>();

        //1.内容
        List<Map> mapList = JSON.parseArray(wmNews.getContent(), Map.class);
        for (Map map : mapList) {
            if ("text".equals(map.get("type"))) {
                sb.append(map.get("value"));
            }

            if ("image".equals(map.get("type"))) {
                imgList.add((String) map.get("value"));
            }
        }

        //2.封面的图片
        if (wmNews.getImages() != null && wmNews.getType() != 0) {
            String[] splitImg = wmNews.getImages().split(",");
            imgList.addAll(Arrays.asList(splitImg));
        }

        //3 标题
        sb.append(wmNews.getTitle());

        resultMap.put("text", sb.toString());
        resultMap.put("images", imgList);
        return resultMap;
    }

    //保存ApArticle
    //保存AppArticle
    private void saveAppArticle(WmNews wmNews) {
        //保存app文章
        ApArticle apArticle = saveArticle(wmNews);
        //保存app文章配置
        saveArticleConfig(apArticle);
        //保存app文章内容
        saveArticleContent(apArticle, wmNews);
        //修改自媒体文章的状态为9
        wmNews.setArticleId(apArticle.getId());
        wmNews.setStatus((short) 9);
        wmNews.setReason("审核通过");
        updateWmNews(wmNews);
        //TODO 创建索引（为后续app端的搜索功能做数据准备）


    }

    //保存文章内容
    private void saveArticleContent(ApArticle apArticle, WmNews wmNews) {
        ApArticleContent apArticleContent = new ApArticleContent();
        apArticleContent.setArticleId(apArticle.getId());
        apArticleContent.setContent(wmNews.getContent());
        articleFeign.saveArticleContent(apArticleContent);
    }

    //保存文章配置信息
    private void saveArticleConfig(ApArticle apArticle) {
        ApArticleConfig apArticleConfig = new ApArticleConfig();
        apArticleConfig.setArticleId(apArticle.getId());
        apArticleConfig.setIsForward(true);
        apArticleConfig.setIsDelete(false);
        apArticleConfig.setIsDown(true);
        apArticleConfig.setIsComment(true);
        articleFeign.saveArticleConfig(apArticleConfig);
    }

    //保存文章
    private ApArticle saveArticle(WmNews wmNews) {
        ApArticle apArticle = new ApArticle();
        apArticle.setTitle(wmNews.getTitle());
        apArticle.setLayout(wmNews.getType());
        apArticle.setImages(wmNews.getImages());
        apArticle.setCreatedTime(new Date());
        apArticle.setPublishTime(new Date());

        //authorId,authorName,channelName
        //获取wmUserId查询ap_author
        Integer wmUserId = wmNews.getUserId();
        ApAuthor apAuthor = articleFeign.findByWmUserId(wmUserId);
        if (apAuthor != null) {
            apArticle.setAuthorId(apAuthor.getId().longValue());
            apArticle.setAuthorName(apAuthor.getName());
        }

        //获取频道相关信息
        Integer channelId = wmNews.getChannelId();
        AdChannel channel = adChannelMapper.selectById(channelId);
        if (channel != null) {
            apArticle.setChannelId(channel.getId());
            apArticle.setChannelName(channel.getName());
        }

        return articleFeign.saveArticle(apArticle);
    }

    //更新文章
    private void updateWmNews(WmNews wmNews) {
        wemediaFeign.updateWmNews(wmNews);
    }
}
