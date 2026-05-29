package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 热门小说模块配置实体
 * 存储热门小说模块6本小说的固定顺序（用于轮播展示）
 */
@Data
@Entity
@Table(name = "hot_novels")
public class HotNovel {

    @Id
    @Column(name = "position")
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
