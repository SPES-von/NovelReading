package com.novelreading.controller;

import com.novelreading.entity.Chapter;
import com.novelreading.service.ChapterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 章节控制器
 * 提供章节列表和内容，用于详情页目录、阅读页正文和听书功能
 * 调用链：前端 chapterApi -> /chapters/** -> ChapterService -> ChapterRepository
 */
@RestController
@RequestMapping("/chapters")
@RequiredArgsConstructor
public class ChapterController {

    private final ChapterService chapterService;

    /**
     * 获取某本小说的章节目录（按 volumeSort、sortOrder 升序）
     * 用途：详情页目录展示、阅读页计算上一章/下一章
     */
    @GetMapping("/novel/{novelId}")
    public ResponseEntity<List<Chapter>> getChaptersByNovel(@PathVariable Long novelId) {
        return ResponseEntity.ok(chapterService.getChaptersByNovelId(novelId));
    }

    /**
     * 按章节ID获取章节详情
     * 用途：阅读页加载标题和正文内容；不存在时返回 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Chapter> getChapter(@PathVariable Long id) {
        Chapter ch = chapterService.getChapterById(id);
        return ch != null ? ResponseEntity.ok(ch) : ResponseEntity.notFound().build();
    }
}
