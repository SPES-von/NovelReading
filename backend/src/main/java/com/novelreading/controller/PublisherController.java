package com.novelreading.controller;

import com.novelreading.entity.Publisher;
import com.novelreading.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 文库控制器
 * 提供文库列表，用于首页分类/文库下拉菜单
 */
@RestController
@RequestMapping("/publishers")
@RequiredArgsConstructor
public class PublisherController {

    private final NovelService novelService;

    @GetMapping
    public ResponseEntity<List<Publisher>> list() {
        return ResponseEntity.ok(novelService.getPublishers());
    }
}
