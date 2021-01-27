package com.wte.apiuser.service;

import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.modules.ums.model.UmsUser;

/**
 * Create by chenglong on 2021/1/21
 */
public interface LoginService {
    String doPwdLogin(String username, String password, String terminal, String ip) throws ApiException;
    String createLoinToken(UmsUser user) throws ApiException;
    Object doMsgLogin(String phoneNum, String msgCode, String terminal, String ip);
}
