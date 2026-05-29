package com.novelreading.dto;

import lombok.Data;

@Data
public class AdminChangePasswordRequest {
    private String oldPassword;
    private String newPassword;
}
