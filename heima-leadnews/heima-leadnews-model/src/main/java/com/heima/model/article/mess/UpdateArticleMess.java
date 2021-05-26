package com.heima.model.article.mess;

import lombok.Data;

@Data
public class UpdateArticleMess {

    /**
     * 修改文章的字段类型
      */
    private UpdateArticleType type;
    /**
     * 文章ID
     */
    private Long articleId;
    /**
     * 修改数据的增量，可为正负
     */
    private Integer add;

    public enum UpdateArticleType{
        //COLLECTION收藏；COMMENT评论；LIKES点赞；VIEWS阅读
        COLLECTION,COMMENT,LIKES,VIEWS;
    }
}