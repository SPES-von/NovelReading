package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 完本推荐模块配置实体
 * 存储完本推荐模块9本小说的固定顺序（前3条是经典完本模块，后6条是完本推荐模块）
 */
@Data
@Entity
@Table(name = "completed_novels")
public class CompletedNovel {

    @Id
    @Column(name = "position")
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
