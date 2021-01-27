package com.wte.apiuser.service;

import com.wt.wte.wtebase.common.api.ApiException;

import java.util.Map;

/**
 * Create by chenglong on 2021/1/19
 */
public interface ValidService {
    Map generateImageCode() throws ApiException;

    String validImageCode(String uuid, String code) throws ApiException;

    String generateMsgCode(String type, String phoneNum) throws ApiException;

    Object validMsgCode(String phoneNum, String code) throws ApiException;
}
