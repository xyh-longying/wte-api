package com.wte.apiuser.controller;

import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.common.api.CommonResult;
import com.wte.apiuser.service.AccountService;
import com.wte.apiuser.service.LoginService;
import com.wte.apiuser.service.ValidService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Create by chenglong on 2021/1/14
 */
@Api(tags = "[0101]账号管理")
@Slf4j
@RestController
@RequestMapping("/api/account")
public class AccountController {
    @Autowired
    private ValidService validService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private LoginService loginService;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010101]生成图形验证码接口")
    @RequestMapping(value = "imageCode/create",method = RequestMethod.GET)
    public CommonResult createImgCode(){
        try {
            return CommonResult.success(validService.generateImageCode());
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01010101",description = "图形验证码错误"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010102]校验图形验证码接口")
    @RequestMapping(value = "imageCode/valid",method = RequestMethod.GET)
    public CommonResult validImgCode(@RequestParam(value = "code") @NonNull @ApiParam("验证码") String code,
                                     @RequestParam(value = "uuid") @NonNull @ApiParam("验证码标识") String uuid){
        try {
            return CommonResult.success(validService.validImageCode(uuid,code));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01010102",description = "手机号发送短信验证码频率过高"),
            @ApiResponse(responseCode = "C01010105",description = "短信验证码发送失败"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010103]生成短信验证码接口")
    @RequestMapping(value = "msgCode/create",method = RequestMethod.GET)
    public CommonResult createMsgCode(@RequestParam(value = "phoneNum") @NonNull @ApiParam("手机号") String phoneNum,
                                      @RequestParam(value = "type") @NonNull @ApiParam("作用类型")  String type){
        try {
            return CommonResult.success(validService.generateMsgCode(type,phoneNum));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01010103",description = "短信验证码错误或已失效"),
            @ApiResponse(responseCode = "C01010104",description = "手机号码不正确"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010104]校验短信验证码接口")
    @RequestMapping(value = "msgCode/valid",method = RequestMethod.GET)
    public CommonResult validMsgCode(@RequestParam(value = "phoneNum") @NonNull @ApiParam("手机号") String phoneNum,
                                      @RequestParam(value = "code") @NonNull @ApiParam("验证码") String code){
        try {
            return CommonResult.success(validService.validMsgCode(phoneNum,code));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01010201",description = "用户名已被注册"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010201]校验用户名是否已注册接口")
    @RequestMapping(value = "username/unique/valid",method = RequestMethod.GET)
    @ResponseBody
    public CommonResult validUsernameUnique(@RequestParam(value = "username") @NonNull @ApiParam("用户名") String username){
        try {
            return CommonResult.success(accountService.validUsernameUnique(username));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01010202",description = "新增用户失败"),
            @ApiResponse(responseCode = "C01020203",description = "新增用户信息失败"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010202]注册用户接口")
    @RequestMapping(value = "create",method = RequestMethod.POST)
    @ResponseBody
    public CommonResult registUser(@RequestParam(value = "username") @NonNull @ApiParam("用户名") String username,
                               @RequestParam(value = "password") @NonNull @ApiParam("密码") String password){
        try {
            return CommonResult.success(accountService.createNewUser(username,password));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01020301",description = "用户名不存在"),
            @ApiResponse(responseCode = "C01020302",description = "登录密码错误"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010301]密码登录")
    @RequestMapping(value = "pwd/login",method = RequestMethod.POST)
    @ResponseBody
    public CommonResult pwdLogin(@RequestParam(value = "username") @NonNull @ApiParam("用户名") String username,
                                   @RequestParam(value = "password") @NonNull @ApiParam("密码") String password,
                                   @RequestParam(value = "terminal") @NonNull @ApiParam("终端") String terminal,
                                   @RequestParam(value = "ip") @NonNull @ApiParam("IP") String ip
                                 ){
        try {
            return CommonResult.success(loginService.doPwdLogin(username,password,terminal,ip));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "C01020303",description = "手机号码未注册"),
            @ApiResponse(responseCode = "C01020304",description = "验证码错误"),
            @ApiResponse(responseCode = "500",description = "接口内部异常")
    })
    @ApiOperation(value = "[01010302]手机验证码登录")
    @RequestMapping(value = "msg/login",method = RequestMethod.POST)
    @ResponseBody
    public CommonResult msgLogin(@RequestParam(value = "phoneNum") @NonNull @ApiParam("手机号") String phoneNum,
                                 @RequestParam(value = "msgCode") @NonNull @ApiParam("验证码") String msgCode,
                                 @RequestParam(value = "terminal") @NonNull @ApiParam("终端") String terminal,
                                 @RequestParam(value = "ip") @NonNull @ApiParam("IP") String ip
    ){
        try {
            return CommonResult.success(loginService.doMsgLogin(phoneNum,msgCode,terminal,ip));
        } catch (ApiException e){
            return CommonResult.failed(e);
        }
    }

}
