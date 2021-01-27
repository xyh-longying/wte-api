package com.wte.apiuser.dto;

import lombok.Builder;
import lombok.Data;

/**
 * Create by chenglong on 2021/1/18
 */
@Data
@Builder
public class User {

    private String uuid;
    private String username;
    private String password;

}
