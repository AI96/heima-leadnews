package com.heima.article.controller.v1;

import com.heima.apis.article.ApArticleConfigControllerApi;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.article.service.ApArticleConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/article_config")
public class ApArticleConfigController implements ApArticleConfigControllerApi {

    @Autowired
    private ApArticleConfigService apArticleConfigService;

    @PostMapping("/save")
    @Override
    public ResponseResult saveArticleConfig(@RequestBody ApArticleConfig apArticleConfig) {
        apArticleConfigService.save(apArticleConfig);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
