package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体
 * 主键为 (username, email)，id 仅用于外键引用
 */
@Data
@Entity
@Table(name = "user")
public class User {

    @EmbeddedId
    private UserId pk = new UserId();

    @Column(name = "id", unique = true, insertable = false, updatable = false)
    private Long internalId;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_banned", nullable = false)
    private Integer isBanned = 0;

    @Column(name = "banned_at")
    private LocalDateTime bannedAt;

    @Column(name = "banned_until")
    private LocalDateTime bannedUntil;

    public String getUsername() {
        return pk != null ? pk.getUsername() : null;
    }

    public void setUsername(String username) {
        if (pk == null) pk = new UserId();
        pk.setUsername(username);
    }

    public String getEmail() {
        return pk != null ? pk.getEmail() : null;
    }

    public void setEmail(String email) {
        if (pk == null) pk = new UserId();
        pk.setEmail(email);
    }

    /** 供外键与前端使用的内部 ID */
    public Long getId() {
        return internalId;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
