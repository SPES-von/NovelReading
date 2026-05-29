package com.novelreading.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理员用户列表项 DTO
 */
@Data
public class AdminUserDTO {
    private Long id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private Integer isBanned;
    private LocalDateTime bannedAt;
    private LocalDateTime bannedUntil;
}
