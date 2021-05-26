package com.heima.apis.article;

import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleControllerApi {

    /**
     * 保存app文章
     * @param apArticle
     * @return
     */
    ApArticle saveArticle(ApArticle apArticle);

}
