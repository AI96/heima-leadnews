package com.heima.article.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.article.pojos.ApAuthor;

public interface ApAuthorService extends IService<ApAuthor> {

    /**
     * 根据wmUserId查询ApAuthor
     * @param wmUserId
     * @return
     */
    ApAuthor findByWmUserId(Integer wmUserId);
}