package com.novelreading.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 章节实体
 * 存储章节信息，支持百度语音合成 API 朗读章节内容
 */
@Data
@Entity
@Table(name = "chapter")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    @JsonIgnore
    private Novel novel;

    @Column(nullable = false, length = 50)
    private String volume = "1";

    @Column(nullable = false, length = 255)
    private String title;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    /** 卷在书籍详情页作者信息下按钮的排序位置，用于选卷按钮按此值从小到大显示 */
    @Column(name = "sort", nullable = true)
    private Integer volumeSort;

    @Column(columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "word_count")
    private Integer wordCount = 0;

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
