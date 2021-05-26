package com.heima.apis.comment;

import com.heima.model.comment.dtos.CommentDto;
import com.heima.model.comment.dtos.CommentLikeDto;
import com.heima.model.comment.dtos.CommentSaveDto;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @Description:
 * @author: yp
 */
public interface CommentControllerApi {

    /**
     * 发表评论
     * @param dto
     * @return
     */
    ResponseResult saveComment(CommentSaveDto dto);


    /**
     * 点赞某一条评论
     * @param dto
     * @return
     */
    public ResponseResult like(CommentLikeDto dto);

    /**
     * 查询评论
     * @param dto
     * @return
     */
    public ResponseResult findByArticleId(CommentDto dto);
}
