package com.novelreading.controller;

import com.novelreading.entity.Novel;
import com.novelreading.entity.Tag;
import com.novelreading.repository.NovelRepository;
import com.novelreading.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员：小说-标签关联（novel_tag 表）的添加与删除
 */
@RestController
@RequestMapping("/admin/novels/{novelId}/tags")
@RequiredArgsConstructor
public class AdminNovelTagController {

    private final NovelRepository novelRepository;
    private final TagRepository tagRepository;

    /** 为小说添加标签：若已存在返回 409，否则插入 novel_tag */
    @PostMapping("/{tagId}")
    @Transactional
    public ResponseEntity<?> add(@PathVariable Long novelId, @PathVariable Long tagId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (novel == null || tag == null) {
            return ResponseEntity.notFound().build();
        }
        boolean alreadyExists = novel.getTags().stream()
                .anyMatch(t -> t.getId().equals(tagId));
        if (alreadyExists) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "该小说存在该标签"));
        }
        novel.getTags().add(tag);
        novelRepository.save(novel);
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 删除小说的该标签：若不存在返回 404，否则从 novel_tag 删除 */
    @DeleteMapping("/{tagId}")
    @Transactional
    public ResponseEntity<?> remove(@PathVariable Long novelId, @PathVariable Long tagId) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) {
            return ResponseEntity.notFound().build();
        }
        boolean removed = novel.getTags().removeIf(t -> t.getId().equals(tagId));
        if (!removed) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "该小说不存在该标签"));
        }
        novelRepository.save(novel);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
