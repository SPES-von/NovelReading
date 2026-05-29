package com.novelreading.dto;

import lombok.Data;

/**
 * 用户个人信息 DTO
 */
@Data
public class UserProfileDTO {

    private Long userId;
    private String username;
    private String nickname;
    private String email;
    private String avatarUrl;
}
