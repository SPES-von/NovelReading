package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 首页推荐榜配置实体
 * 存储首页6本推荐书籍的固定顺序
 */
@Data
@Entity
@Table(name = "home_recommend")
public class HomeRecommend {

    @Id
    @Column(name = "position")
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
