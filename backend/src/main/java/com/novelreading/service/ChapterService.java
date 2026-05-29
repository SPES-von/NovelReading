package com.novelreading.service;

import com.novelreading.entity.Chapter;
import com.novelreading.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 章节服务
 * 提供章节列表和内容，支持阅读页面和百度语音合成朗读
 * 该层负责定义章节读取规则（排序、空值返回约定），供 Controller 直接调用
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterService {

    private final ChapterRepository chapterRepository;

    /** 按小说ID查询全书章节，并按卷顺序+章序返回，保证前端目录与阅读顺序一致 */
    public List<Chapter> getChaptersByNovelId(Long novelId) {
        return chapterRepository.findByNovelIdOrderByVolumeSortAscSortOrderAsc(novelId);
    }

    /** 按章节ID查询正文；未找到返回 null，由 Controller 转换为 404 */
    public Chapter getChapterById(Long id) {
        return chapterRepository.findById(id).orElse(null);
    }
}
