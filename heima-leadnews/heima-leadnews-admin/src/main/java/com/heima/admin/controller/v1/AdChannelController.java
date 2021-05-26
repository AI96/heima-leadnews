package com.heima.admin.controller.v1;

import com.heima.admin.service.AdChannelService;
import com.heima.apis.admin.AdChannelControllerApi;
import com.heima.model.admin.dtos.ChannelDto;
import com.heima.model.admin.pojos.AdChannel;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Description:
 * @author: yp
 */
@RestController
@RequestMapping("/api/v1/channel")
public class AdChannelController implements AdChannelControllerApi {


    @Autowired
    private AdChannelService channelService;

    /**
     * 分页条件查询频道
     * @param channelDto
     * @return
     */
    @PostMapping("/list")
    @Override
    public ResponseResult findPage(@RequestBody ChannelDto channelDto) {
        return channelService.findPage(channelDto);
    }

    /**
     * 频道新增
     * @param channel
     * @return
     */
    @Override
    @PostMapping("/save")
    public ResponseResult save(@RequestBody AdChannel channel) {
        return channelService.insert(channel);
    }

    /**
     * 频道修改
     * @param adChannel
     * @return
     */
    @Override
    @PostMapping("/update")
    public ResponseResult update(@RequestBody AdChannel adChannel) {
        return channelService.update(adChannel);
    }


    /**
     * 频道删除
     * @param id
     * @return
     */
    @Override
    @DeleteMapping("/del/{id}")
    public ResponseResult deleteById(@PathVariable("id") Integer id) {
        return channelService.deleteById(id);
    }

    @GetMapping("/channels")
    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(channelService.list());
    }




}
