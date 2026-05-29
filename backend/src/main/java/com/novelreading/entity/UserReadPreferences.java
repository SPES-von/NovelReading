package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户阅读偏好实体
 */
@Data
@Entity
@Table(name = "user_read_preferences")
public class UserReadPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false, unique = true)
    private User user;

    @Column(name = "font_size")
    private Integer fontSize = 16;

    @Column(name = "line_height", precision = 3, scale = 1)
    private BigDecimal lineHeight = BigDecimal.valueOf(1.8);

    @Column(name = "theme", length = 20)
    private String theme = "default";

    @Column(name = "page_width")
    private Integer pageWidth = 720;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
