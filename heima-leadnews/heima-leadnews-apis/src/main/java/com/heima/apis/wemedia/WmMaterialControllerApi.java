package com.heima.apis.wemedia;

import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import org.springframework.web.multipart.MultipartFile;

public interface WmMaterialControllerApi {


    /**
     * 取消收藏
     * @param id
     * @return
     */
    ResponseResult cancleCollectionMaterial(Integer id);

    /**
     * 收藏
     * @param id
     * @return
     */
    ResponseResult collectionMaterial(Integer id);

    /**
     * 删除素材
     * @param id
     * @return
     */
    ResponseResult delPicture(Integer id);

    /**
     * 上传图片
     * @param multipartFile
     * @return
     */
    ResponseResult uploadPicture(MultipartFile multipartFile);

    /**
     * 素材列表
     * @param dto
     * @return
     */
    ResponseResult findList(WmMaterialDto dto);

}