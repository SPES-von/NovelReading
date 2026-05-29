package com.novelreading.entity;

import jakarta.persistence.*;
import lombok.Data;

/**
 * 新书榜配置实体
 * 存储新书榜10本书籍的固定顺序
 */
@Data
@Entity
@Table(name = "new_book_chart")
public class NewBookChart {

    @Id
    @Column(name = "position")
    private Integer position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "novel_id", nullable = false)
    private Novel novel;
}
