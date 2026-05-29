package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 热点榜配置实体
 * 存储热点榜固定顺序，结构与 new_book_chart 一致
 */
@Data
@Entity
@Table(name = "hot_chart")
public class HotChart {

    @Id
    @Column(name = "position")
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
