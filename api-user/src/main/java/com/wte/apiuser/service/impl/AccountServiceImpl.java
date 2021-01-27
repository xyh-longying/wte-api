package com.wte.apiuser.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wt.wte.wtebase.base.BaseConstants;
import com.wt.wte.wtebase.base.BaseService;
import com.wt.wte.wtebase.base.BaseUtils;
import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.common.api.ExceptionConstants;
import com.wt.wte.wtebase.modules.ums.model.UmsUser;
import com.wt.wte.wtebase.modules.ums.model.UmsUserInfo;
import com.wt.wte.wtebase.modules.ums.service.UmsUserInfoService;
import com.wt.wte.wtebase.modules.ums.service.UmsUserService;
import com.wte.apiuser.dto.User;
import com.wte.apiuser.service.AccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Create by chenglong on 2021/1/15
 */
@Slf4j
@Service
public class AccountServiceImpl extends BaseService implements AccountService {
    @Resource
    private UmsUserService umsUserService;
    @Resource
    private UmsUserInfoService umsUserInfoService;

    @Override
    public String validUsernameUnique(String username) throws ApiException{
        validUniqueUsername(username);
        return "用户名可用";
    }

    @Override
    public User createNewUser(String username, String password) throws ApiException {
        validUniqueUsername(username);
        UmsUser umsUser = createUser(username,password);
        UmsUserInfo umsUserInfo = createUserInfo(umsUser.getUuid());
        JSONObject userJson = new JSONObject(umsUser);
        JSONObject userInfoJson = new JSONObject(umsUserInfo);
        userJson.putAll(userInfoJson);
        return JSONUtil.toBean(userJson, User.class);
    }

    /**
     * 新增用户信息
     * @param userId
     * @return
     */
    private UmsUserInfo createUserInfo(String userId) {
        UmsUserInfo userInfo = UmsUserInfo.builder().userId(userId).build();
        userInfo = createModel(UmsUserInfo.class, null, BeanUtil.beanToMap(userInfo));
        if(umsUserInfoService.save(userInfo)){
            return userInfo;
        } else {
            throw new ApiException(ExceptionConstants.INSERT_USER_INFO_ERROR, null);
        }
    }

    /**
     * 创建用户
     * @param username
     * @param password
     * @return
     */
    private UmsUser createUser(String username, String password) {
        UmsUser user = UmsUser.builder().username(username).password(BaseUtils.bcryptPwd(password)).status(BaseConstants.USER_STATUS_NORMAL).build();
        user = createModel(UmsUser.class, null, BeanUtil.beanToMap(user));
        if(umsUserService.save(user)){
            return user;
        }else {
            throw new ApiException(ExceptionConstants.INSERT_USER_ERROR, null);
        }
    }

    /**
     * 校验用户名是否唯一
     * @param username
     */
    private void validUniqueUsername(String username) {
        QueryWrapper<UmsUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(UmsUser::getIsDelete, BaseConstants.DEL_NO)
                .eq(UmsUser::getUsername,username);
        Integer countNum = umsUserService.count(wrapper);
        if(countNum>0){
            throw new ApiException(ExceptionConstants.USERNAME_NOT_UNIQUE, MapUtil.of("username", username));
        }
    }
}
