package com.wte.apiuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * scanBasePackages增加扫描路径，方便注入bean
 */
@SpringBootApplication(scanBasePackages = {"com.wte.apiuser","com.wt.wte.wtebase"})
public class ApiUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiUserApplication.class, args);
    }

}
