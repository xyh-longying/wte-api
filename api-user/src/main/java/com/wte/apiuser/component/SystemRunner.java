package com.wte.apiuser.component;

import com.wt.wte.wtebase.common.utils.SystemUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Create by chenglong on 2021/1/20
 */
@Component
@Order(1)
@Slf4j
public class SystemRunner implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            SystemUtil.setSystemSettings();
        } catch (Exception e){
            log.info("启动程序自动装配系统配置异常，异常原因：{}",e.getMessage());
        }
        log.info("启动程序自动装配系统配置完成");
    }
}
