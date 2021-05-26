package com.heima.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUserRealname;

/**
 * @Description:
 * @author: yp
 */
public interface ApUserRealnameService extends IService<ApUserRealname> {

    /**
     * APP用户审核列表
     * @param dto
     * @return
     */
    ResponseResult loadListByStatus(AuthDto dto);

    /**
     * APP用户审核
     * @param dto
     * @param authType
     * @return
     */
    ResponseResult updateStatusById(AuthDto dto, Short authType);
}
