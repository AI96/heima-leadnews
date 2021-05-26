package com.heima.wemedia.controller.v1;

import com.heima.apis.wemedia.WmMaterialControllerApi;
import com.heima.common.constans.wemedia.WemediaContans;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.wemedia.service.WmMaterialService;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/material")
public class WmMaterialController implements WmMaterialControllerApi {
    @Autowired
    private WmMaterialService materialService;

    /**
     * 取消收藏
     * @param id
     * @return
     */
    @GetMapping("/cancel_collect/{id}")
    @Override
    public ResponseResult cancleCollectionMaterial(@PathVariable("id") Integer id) {
        return materialService.updateStatus(id, WemediaContans.CANCEL_COLLECT_MATERIAL);
    }

    /**
     * 收藏
     * @param id
     * @return
     */
    @GetMapping("/collect/{id}")
    @Override
    public ResponseResult collectionMaterial(@PathVariable("id")Integer id) {
        return  materialService.updateStatus(id, WemediaContans.COLLECT_MATERIAL);
    }

    /**
     * 删除素材
     * @param id
     * @return
     */
    @GetMapping("/del_picture/{id}")
    @Override
    public ResponseResult delPicture(@PathVariable("id") Integer id) {
        return materialService.delPicture(id);
    }


    /**
     * 素材列表
     * @param dto
     * @return
     */
    @PostMapping("/list")
    @Override
    public ResponseResult findList(@RequestBody WmMaterialDto dto) {
        return materialService.findList(dto);
    }



    @PostMapping("/upload_picture")
    @Override
    public ResponseResult uploadPicture(MultipartFile multipartFile) {
        return materialService.uploadPicture(multipartFile);
    }


}