package com.novelreading.dto;

import lombok.Data;

import java.util.List;

/**
 * 文库下的小说列表 DTO
 * 用于首页"热门小说"中各文库模块展示
 */
@Data
public class PublisherNovelDTO {

    private Long publisherId;
    private String publisherName;
    private List<NovelSimpleDTO> novels;
}
