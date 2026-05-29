package com.novelreading.dto;

import lombok.Data;

/**
 * 用户信息更新请求
 */
@Data
public class UserProfileUpdateRequest {

    private String username;
    private String nickname;
    private String email;
    /** 新密码，不传则不修改 */
    private String newPassword;
}
