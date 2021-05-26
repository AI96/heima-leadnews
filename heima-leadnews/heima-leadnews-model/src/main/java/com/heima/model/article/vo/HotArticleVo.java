package com.heima.model.article.vo;

import com.baomidou.mybatisplus.core.conditions.interfaces.Compare;
import com.heima.model.article.pojos.ApArticle;
import lombok.Data;

@Data
public class HotArticleVo extends ApArticle {

    /**
     * 分值
     */
    private Integer score;

}
