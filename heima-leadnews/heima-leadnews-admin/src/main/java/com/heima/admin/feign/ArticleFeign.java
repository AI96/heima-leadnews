package com.heima.admin.feign;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.article.pojos.ApAuthor;
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
@FeignClient("leadnews-article")
public interface ArticleFeign {

    //根据wmUserId查询ApAuthor
    @GetMapping("/api/v1/author/findByWmUserId/{id}")
    ApAuthor findByWmUserId(@PathVariable(value = "id") Integer wmUserId);

    //保存Aparticle
    @PostMapping("/api/v1/article/save")
    ApArticle saveArticle(@RequestBody ApArticle apArticle);

    //保存ArticleContent
    @PostMapping("/api/v1/article_content/save")
    ResponseResult saveArticleContent(@RequestBody ApArticleContent apArticleContent);

    //保存ArticleConfig
    @PostMapping("/api/v1/article_config/save")
    ResponseResult saveArticleConfig(@RequestBody ApArticleConfig apArticleConfig);

}
