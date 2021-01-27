package com.wte.apiuser.service;

import com.wt.wte.wtebase.common.api.ApiException;
import com.wte.apiuser.dto.User;

/**
 * Create by chenglong on 2021/1/15
 */
public interface AccountService {

    public String validUsernameUnique(String username) throws ApiException;

    public User createNewUser(String username, String password) throws ApiException;
}
