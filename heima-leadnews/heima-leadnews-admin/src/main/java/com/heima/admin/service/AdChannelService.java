package com.heima.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;

/**
 * @Description:
 * @author: yp
 */
public interface AdChannelService extends IService<AdChannel> {

    ResponseResult findPage(ChannelDto channelDto);

    ResponseResult insert(AdChannel channel);

    ResponseResult update(AdChannel adChannel);

    ResponseResult deleteById(Integer id);
}
