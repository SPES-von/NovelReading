package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 强推榜配置实体
 * 存储强推榜固定顺序，结构与 new_book_chart 一致
 */
@Data
@Entity
@Table(name = "strong_recommend_chart")
public class StrongRecommendChart {

    @Id
    @Column(name = "position")
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
