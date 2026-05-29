package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 小说实体
 * 存储小说核心信息，用于首页特色推荐、热门小说、完本推荐等模块
 */
@Data
@Entity
@Table(name = "novel")
public class Novel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private Publisher publisher;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(columnDefinition = "TEXT")
    private String synopsis;

    @Column(name = "word_count")
    private Integer wordCount = 0;

    @Column(precision = 3, scale = 1)
    private BigDecimal rating = BigDecimal.ZERO;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "is_featured")
    private Boolean isFeatured = false;

    @Column(name = "is_new_select")
    private Boolean isNewSelect = false;

    @Column(name = "new_select_sort")
    private Integer newSelectSort = 0;

    @Column(name = "view_count")
    private Long viewCount = 0L;

    @Column(name = "recommend_count")
    private Integer recommendCount = 0;

    @Column(name = "flower_count")
    private Integer flowerCount = 0;

    @Column(name = "favorite_count")
    private Integer favoriteCount = 0;

    /** 是否动漫化：0-未动漫化，1-已动漫化 */
    @Column(name = "is_animated")
    private Integer isAnimated = 0;

    @Column(name = "latest_chapter_title", length = 255)
    private String latestChapterTitle;

    @Column(name = "latest_chapter_id")
    private Long latestChapterId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "novel_tag",
        joinColumns = @JoinColumn(name = "novel_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

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
