package com.novelreading.dto;

import lombok.Data;

/**
 * 封禁用户请求：封禁时长（小时）
 */
@Data
public class AdminUserBanRequest {
    /** 封禁时长（小时），如 24, 64, 8760(1年) 等 */
    private Long durationHours;
}
