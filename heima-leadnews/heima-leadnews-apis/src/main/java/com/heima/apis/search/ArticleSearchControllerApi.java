package com.heima.apis.search;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.search.dtos.UserSearchDto;

import java.io.IOException;

public interface ArticleSearchControllerApi {
    /**
     *  搜索文章
     * @param userSearchDto
     * @return
     */
    ResponseResult search(UserSearchDto userSearchDto) throws IOException;
}