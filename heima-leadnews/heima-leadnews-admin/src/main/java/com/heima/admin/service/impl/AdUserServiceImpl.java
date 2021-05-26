package com.heima.admin.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.heima.admin.mapper.AdUserMapper;
import com.heima.admin.service.AdUserService;
import com.heima.model.admin.dtos.AdUserDto;
import com.heima.model.admin.pojos.AdUser;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.utils.common.AppJwtUtil;
import com.heima.utils.common.MD5Utils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Map;

/**
 * @Description:
 * @author: yp
 */
@Service
@Slf4j
public class AdUserServiceImpl extends ServiceImpl<AdUserMapper, AdUser> implements AdUserService {

    @Autowired
    private AdUserMapper adUserMapper;

    @Override
    public ResponseResult login(AdUserDto dto) {
        //1.参数检查
        if(dto==null || StringUtils.isEmpty(dto.getName()) || StringUtils.isEmpty(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //2.根据用户名查询出user
        AdUser adUser = adUserMapper.selectOne(Wrappers.<AdUser>lambdaQuery().eq(AdUser::getName, dto.getName()));
        if(adUser==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }
        //3.取出盐,把前端传过来的密码和盐进行加密
        String salt = adUser.getSalt();
        String enPassword = MD5Utils.encodeWithSalt(dto.getPassword(), salt);

        //4.把加密后的密码和数据库进行比对
        if(!enPassword.equals(adUser.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        //5.一致, 生成token
        //6.封装数据
        Map<String, Object> map = Maps.newHashMap();
        adUser.setPassword("");
        adUser.setSalt("");
        map.put("token", AppJwtUtil.getToken(adUser.getId().longValue()));
        map.put("user", adUser);
        return ResponseResult.okResult(map);
    }
}
