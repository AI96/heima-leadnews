package com.heima.apis.article;

import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * @Description:
 * @author: yp
 */
public interface AuthorControllerApi {
    /**
     *根据用户id查询作者信息
     * @param id
     * @return
     */
    public ApAuthor findByUserId(@PathVariable("id") Integer id);

    /**
     * 保存作者
     * @param apAuthor
     * @return
     */
    public ResponseResult save(@RequestBody ApAuthor apAuthor);

    /**
     * 根据WmUserId查询ApAuthor
     * @param wmUserId
     * @return
     */
    public ApAuthor findByWmUserId(Integer wmUserId);
}
