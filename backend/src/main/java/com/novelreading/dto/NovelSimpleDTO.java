package com.novelreading.dto;

import lombok.Data;

/**
 * 小说简要信息 DTO
 * 用于列表展示，如完本推荐、文库列表等
 */
@Data
public class NovelSimpleDTO {

    private Long id;
    private String title;
    private String authorName;
    private String publisherName;
    private String coverUrl;
    private String synopsis;
    private String tagName;  // 单个标签，如"电击文库"、"青春日常"
}
