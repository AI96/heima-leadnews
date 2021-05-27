package com.heima.article.controller.v1;

import com.heima.apis.article.ApArticleControllerApi;
import com.heima.model.article.pojos.ApArticle;
import com.heima.article.service.ApArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article")
public class ApArticleController implements ApArticleControllerApi {

    @Autowired
    private ApArticleService articleService;

    @PostMapping("/save")
    @Override
    public ApArticle saveArticle(@RequestBody ApArticle apArticle) {
        articleService.save(apArticle);
        return apArticle;
    }
}
