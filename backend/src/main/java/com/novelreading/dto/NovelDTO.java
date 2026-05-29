package com.novelreading.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 小说数据传输对象
 * 用于 API 返回，包含作者名、文库名等关联信息
 */
@Data
public class NovelDTO {

    private Long id;
    private String title;
    private String authorName;
    private Long authorId;
    private String publisherName;
    private Long publisherId;
    private String coverUrl;
    private String synopsis;
    private Integer wordCount;
    private BigDecimal rating;
    private Boolean isCompleted;
    private Long viewCount;
    private Integer recommendCount;
    private Integer flowerCount;
    private Integer favoriteCount;
    /** 章节数（该书在 chapter 表中的记录数） */
    private Integer chapterCount;
    /** 是否动漫化：0-未动漫化，1-已动漫化 */
    private Integer isAnimated;
    private String latestChapterTitle;
    private List<String> tagNames;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
