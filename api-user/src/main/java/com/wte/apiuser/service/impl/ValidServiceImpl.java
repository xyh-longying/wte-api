package com.wte.apiuser.service.impl;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.ShearCaptcha;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wt.wte.wtebase.base.BaseService;
import com.wt.wte.wtebase.base.BaseUtils;
import com.wt.wte.wtebase.common.api.ApiException;
import com.wt.wte.wtebase.common.api.ExceptionConstants;
import com.wt.wte.wtebase.common.constants.SystemSettingKey;
import com.wt.wte.wtebase.common.service.RedisService;
import com.wt.wte.wtebase.common.utils.ShortMessageUtil;
import com.wt.wte.wtebase.common.utils.SystemUtil;
import com.wt.wte.wtebase.nosql.mongodb.document.MsgCodeSendRecord;
import com.wt.wte.wtebase.nosql.mongodb.service.MsgCodeSendRecordService;
import com.wte.apiuser.service.ValidService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Map;

/**
 * Create by chenglong on 2021/1/19
 */
@Slf4j
@Service
public class ValidServiceImpl extends BaseService implements ValidService {

    @Autowired
    private RedisService redisService;
    @Resource
    private MsgCodeSendRecordService msgCodeSendRecordService;
    @Value("${redis.key.prefix.imgCode}")
    private String REDIS_KEY_IMGCODE;
    @Value("${redis.key.expire.imgCode}")
    private Long IMGCODE_EXPIRE_TIME;
    @Value("${redis.key.prefix.msgCode}")
    private String REDIS_KEY_MSGCODE;

    @Override
    public Map generateImageCode() {
        ShearCaptcha captcha = CaptchaUtil.createShearCaptcha(120, 40, 4, 4);
        String uuid = UUID.randomUUID().toString();
        redisService.set(REDIS_KEY_IMGCODE+"_"+uuid, captcha.getCode(), IMGCODE_EXPIRE_TIME);
        Map map = MapUtil.of(new String[][]{
                {"uuid",uuid},
                {"codeBase64",captcha.getImageBase64()},
        });
        return map;
    }

    @Override
    public String validImageCode(String uuid, String code) {
        String imgCode = (String) redisService.get(REDIS_KEY_IMGCODE+"_"+uuid);
        if(StrUtil.equals(imgCode, code)){
            redisService.del(REDIS_KEY_IMGCODE+"_"+uuid);
            return "图形验证码正确";
        }else{
            throw new ApiException(ExceptionConstants.IMGCODE_NOT_CORRECT,null);
        }
    }

    @Override
    public String generateMsgCode(String type, String phoneNum) {
        if(!BaseUtils.isPhoneNum(phoneNum)){
            throw new ApiException(ExceptionConstants.PHONENUM_WRONG, MapUtil.of("phoneNum",phoneNum));
        }
        validMsgCodeFrequency(type,phoneNum);
        removeMsgCodeCache(phoneNum);
        createAndSendMsgCodeStr(type,phoneNum);
        return "已发送短信验证码";
    }

    @Override
    public String validMsgCode(String phoneNum, String code) {
        String key = REDIS_KEY_MSGCODE+"_"+phoneNum;
        String msgCode = (String) redisService.get(key);
        if(StrUtil.equals(msgCode, code)){
            redisService.del(key);
            return "短信验证码正确";
        }else{
            throw new ApiException(ExceptionConstants.MSGCODE_NOT_CORRECT,null);
        }
    }

    /**
     * 校验短信验证码发送频率
     * @param type
     * @param phoneNum
     */
    private void validMsgCodeFrequency(String type, String phoneNum) {
        int maxNum = NumberUtil.parseInt(SystemUtil.getSettingValue(SystemSettingKey.MSGCODE_FREQUENCY_NUM));
        long num = msgCodeSendRecordService.countMsgCodeSendRecordsByPhoneNumLastOneMinute(phoneNum, type, DateUtil.date());
        if(num > maxNum){
            log.info("手机号{}最近1分钟已频发送短信验证码超过{}次，无法再次发送",phoneNum,maxNum);
            throw new ApiException(ExceptionConstants.MSGCODE_SEND_FREQUENCY, MapUtil.of("phoneNum", phoneNum));
        }
    }

    /**
     * 移除缓存中已存在的手机验证码
     * @param phoneNum
     */
    private void removeMsgCodeCache(String phoneNum){
        String key = REDIS_KEY_MSGCODE+"_"+phoneNum;
        if(redisService.hasKey(key)){
            redisService.del(key);
        }
    }

    /**
     * 生成并发送短信验证码
     * @param type
     * @param phoneNum
     */
    private void createAndSendMsgCodeStr(String type, String phoneNum){
        String key = REDIS_KEY_MSGCODE+"_"+phoneNum;
        String expire = SystemUtil.getSettingValue(SystemSettingKey.MSGCODE_EXPIRE);
        String code = RandomUtil.randomNumbers(6);
        String msg = ShortMessageUtil.sendCodeMessage(phoneNum, code, type);
        if(StrUtil.isNotEmpty(msg)){
            log.info("生成的短信验证码信息内容：{}", msg);
        }
        MsgCodeSendRecord record = MsgCodeSendRecord.builder().id(IdUtil.simpleUUID()).phoneNum(phoneNum).type(type).code(code).msg(msg).sendTime(DateUtil.date()).build();
        msgCodeSendRecordService.saveMsgCodeSendRecords(record);
        redisService.set(key, code, NumberUtil.parseLong(expire)*60);
    }
}
