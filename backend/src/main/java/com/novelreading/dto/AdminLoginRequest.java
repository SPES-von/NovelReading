package com.novelreading.dto;

import lombok.Data;

/**
 * 管理员登录请求 DTO
 */
@Data
public class AdminLoginRequest {

    /** 管理员 id（登录账号） */
    private Long adminId;
    /** 密码 */
    private String password;
}
