package com.heima.apis.behavior;

import com.heima.model.behavior.dtos.LikesBehaviorDto;
import com.heima.model.behavior.pojos.ApLikesBehavior;
import com.heima.model.common.dtos.ResponseResult;

public interface ApLikesBehaviorControllerApi {

   /**
     * 保存点赞行为
     * @param dto
     * @return
     */
	ResponseResult like(LikesBehaviorDto dto);

}