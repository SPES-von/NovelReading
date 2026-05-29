package com.novelreading.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户表复合主键 (username, email)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class UserId implements Serializable {

    @Column(name = "username", nullable = false, length = 50)
    private String username;

    @Column(name = "email", nullable = false, length = 100)
    private String email;
}
