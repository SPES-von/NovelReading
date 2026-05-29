package com.novelreading.dto;

import lombok.Data;

/**
 * 登录/注册响应 DTO
 * 包含 JWT token 和用户信息
 */
@Data
public class AuthResponse {

    private String token;
    private String username;
    private String nickname;
    private Long userId;
    private String avatarUrl;
}
