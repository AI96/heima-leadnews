package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.AuthorMapper;
import com.heima.article.service.ApAuthorService;
import com.heima.model.article.pojos.ApAuthor;
import org.springframework.stereotype.Service;

@Service
public class ApAuthorServiceImpl extends ServiceImpl<AuthorMapper, ApAuthor> implements ApAuthorService {
    @Override
    public ApAuthor findByWmUserId(Integer wmUserId) {
        LambdaQueryWrapper<ApAuthor> queryWrapper = new LambdaQueryWrapper<ApAuthor>();
        queryWrapper.eq(ApAuthor::getWmUserId,wmUserId);
        ApAuthor apAuthor = getOne(queryWrapper);
        return apAuthor;
    }
}