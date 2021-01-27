package com.wte.apiuser.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wt.wte.wtebase.base.BaseConstants;
import com.wt.wte.wtebase.base.BaseService;
import com.wt.wte.wtebase.base.BaseUtils;
import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.common.api.ExceptionConstants;
import com.wt.wte.wtebase.common.dto.UmsUserDetails;
import com.wt.wte.wtebase.common.service.RedisService;
import com.wt.wte.wtebase.common.utils.JwtTokenUtil;
import com.wt.wte.wtebase.modules.ums.model.UmsLoginInfo;
import com.wt.wte.wtebase.modules.ums.model.UmsUser;
import com.wt.wte.wtebase.modules.ums.service.UmsLoginInfoService;
import com.wt.wte.wtebase.modules.ums.service.UmsUserService;
import com.wte.apiuser.service.LoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Create by chenglong on 2021/1/21
 */
@Service
@Slf4j
public class LoginServiceImpl extends BaseService implements LoginService {
    @Autowired
    private UmsUserService umsUserService;
    @Autowired
    private UmsLoginInfoService umsLoginInfoService;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisService redisService;
    @Value("${redis.key.prefix.msgCode}")
    private String REDIS_KEY_MSGCODE;

    @Override
    public String doPwdLogin(String username, String password, String terminal, String ip) throws ApiException {
        QueryWrapper<UmsUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(UmsUser::getIsDelete, BaseConstants.DEL_NO)
                .eq(UmsUser::getUsername, username);
        UmsUser umsUser = umsUserService.getOne(wrapper);
        if(umsUser==null){
            throw new ApiException(ExceptionConstants.USERNAME_NOT_EXIST, MapUtil.of("username", username));
        }
        if(!BaseUtils.checkPwd(password, umsUser.getPassword())){
            throw new ApiException(ExceptionConstants.PASSWORD_WRONG, null);
        }
        saveLoginRecord(umsUser,terminal,ip);
        return createLoinToken(umsUser);
    }

    private void saveLoginRecord(UmsUser umsUser, String terminal, String ip) {
        UmsLoginInfo loginInfo = UmsLoginInfo.builder()
                .userId(umsUser.getUuid())
                .username(umsUser.getUsername())
                .terminal(terminal)
                .ip(ip)
                .address("")
                .loginTime(DateUtil.date())
                .build();
        loginInfo = createModel(UmsLoginInfo.class, null, BeanUtil.beanToMap(loginInfo));
        umsLoginInfoService.save(loginInfo);
    }

    @Override
    public String createLoinToken(UmsUser user) throws ApiException {
        return jwtTokenUtil.generateToken(new UmsUserDetails(user));
    }

    @Override
    public Object doMsgLogin(String phoneNum, String msgCode, String terminal, String ip) {
        QueryWrapper<UmsUser> wrapper = new QueryWrapper<>();
        wrapper.lambda()
                .eq(UmsUser::getIsDelete, BaseConstants.DEL_NO)
                .eq(UmsUser::getMobile, phoneNum);
        UmsUser umsUser = umsUserService.getOne(wrapper);
        if(umsUser==null){
            throw new ApiException(ExceptionConstants.PHONENUM_NOT_EXIST, MapUtil.of("phoneNum", phoneNum));
        }
        String key = REDIS_KEY_MSGCODE+"_"+phoneNum;
        String cacheCode = (String) redisService.get(key);
        if(!StrUtil.equals(msgCode, cacheCode)){
            throw new ApiException(ExceptionConstants.MSGCODE_WRONG, null);
        }
        saveLoginRecord(umsUser,terminal,ip);
        return createLoinToken(umsUser);
    }
}
