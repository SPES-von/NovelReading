package com.novelreading.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户阅读偏好 DTO
 */
@Data
public class UserReadPreferencesDTO {

    private Integer fontSize = 16;
    private BigDecimal lineHeight = BigDecimal.valueOf(1.8);
    private String theme = "default";
    private Integer pageWidth = 720;
}
