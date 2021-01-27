package com.wte.apiuser.controller;

import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.common.api.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create by chenglong on 2021/1/22
 */
@Api(tags = "[0102]用户管理")
@Slf4j
@RestController
@RequestMapping("/api/userinfo")
public class UserInfoController {

    @ApiOperation(value = "[01020101]更新用户基本信息")
    @RequestMapping(value = "info/update",method = RequestMethod.GET)
    public CommonResult updateUserInfo(){
        try {
            return CommonResult.success("更新成功");
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }
}
