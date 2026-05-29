package com.novelreading.dto;

import lombok.Data;

/**
 * 管理员登录响应 DTO
 */
@Data
public class AdminAuthResponse {

    private String token;
    private Long adminId;
}
