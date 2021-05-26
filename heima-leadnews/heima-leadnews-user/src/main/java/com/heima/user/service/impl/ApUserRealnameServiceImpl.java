package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constans.user.UserConstants;
import com.heima.model.article.pojos.ApAuthor;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.AuthDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.model.user.pojos.ApUserRealname;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.user.feign.ArticleFeign;
import com.heima.user.feign.WemediaFeign;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.mapper.ApUserRealnameMapper;
import com.heima.user.service.ApUserRealnameService;
import io.seata.spring.annotation.GlobalTransactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.metrics.web.servlet.WebMvcMetricsFilter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @Description:
 * @author: yp
 */
@Service
public class ApUserRealnameServiceImpl extends ServiceImpl<ApUserRealnameMapper, ApUserRealname> implements ApUserRealnameService {

    @Override
    public ResponseResult loadListByStatus(AuthDto dto) {
        //1.检查参数
        if(dto == null ){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页检查
        dto.checkParam();

        //2.根据状态分页查询
        LambdaQueryWrapper<ApUserRealname> lambdaQueryWrapper = new LambdaQueryWrapper();
        if(dto.getStatus() != null){
            lambdaQueryWrapper.eq(ApUserRealname::getStatus,dto.getStatus());
        }
        //分页条件构建
        IPage pageParam = new Page(dto.getPage(),dto.getSize());
        IPage page = page(pageParam, lambdaQueryWrapper);

        //3.返回结果
        PageResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());
        return responseResult;
    }


    @Autowired
    private ApUserMapper apUserMapper;

    @Autowired
    private ArticleFeign articleFeign;

    @Autowired
    private WemediaFeign wemediaFeign;

    /**
     * APP端用户审核
     * @param dto
     * @param authType
     * @return
     */
    //@Transactional
    //@GlobalTransactional

    @Override
    public ResponseResult updateStatusById(AuthDto dto, Short authType) {
        //1.参数检查
        if(dto==null || dto.getId()==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        //2.更新ap_user_realname
        ApUserRealname apUserRealname = getById(dto.getId());
        if (apUserRealname==null){
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST,"数据不存在");
        }
        apUserRealname.setStatus(authType);
        if(StringUtils.isNotEmpty(dto.getMsg())){
            apUserRealname.setReason(dto.getMsg());
        }
        apUserRealname.setUpdatedTime(new Date());
        updateById(apUserRealname);

        //3.审核通过
        if(UserConstants.PASS_AUTH.equals(authType)){
            //3.1 创建自媒体账户
            ApUser apUser = apUserMapper.selectById(apUserRealname.getUserId());
            if(apUser==null){
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
            }
            WmUser wmUser =  createWmUser(apUser);
            if(wmUser == null){
                throw new RuntimeException("自媒体用户创建失败");
            }

            int i = 1/0;

            //3.2 创建文章作者
            createAuthor(wmUser);

            //3.3更新apUser
            apUser.setFlag((short)1);
            apUserMapper.updateById(apUser);
        }
        return ResponseResult.okResult(null);
    }

    /**
     * 创建文章作者
     * @param wmUser
     */
    private void createAuthor(WmUser wmUser) {
        Integer apUserId = wmUser.getApUserId();
        ApAuthor apAuthor =  articleFeign.findByUserId(apUserId);
        if (apAuthor == null) {
            apAuthor = new ApAuthor();
            apAuthor.setName(wmUser.getName());
            apAuthor.setType(UserConstants.AUTH_TYPE);
            apAuthor.setCreatedTime(new Date());
            apAuthor.setUserId(apUserId);
            apAuthor.setWmUserId(wmUser.getId());
            articleFeign.save(apAuthor);
        }
    }

    /**
     * 创建自媒体账户
     * @param apUser
     * @return
     */
    private WmUser createWmUser(ApUser apUser) {
        WmUser wmUser = wemediaFeign.findByName(apUser.getName());
        if (wmUser == null || wmUser.getId()==null) {
            wmUser = new WmUser();
            //设置ApUserId
            wmUser.setApUserId(apUser.getId());
            wmUser.setCreatedTime(new Date());
            wmUser.setSalt(apUser.getSalt());
            wmUser.setName(apUser.getName());
            wmUser.setPassword(apUser.getPassword());
            wmUser.setStatus(9);
            wmUser.setPhone(apUser.getPhone());
            wemediaFeign.save(wmUser);
        }
        return wmUser;
    }
}
