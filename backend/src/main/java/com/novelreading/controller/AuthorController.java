package com.novelreading.controller;

import com.novelreading.entity.Author;
import com.novelreading.repository.AuthorRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 作者控制器
 * 提供作者列表数据，用于后台上架页作者下拉选择
 */
@RestController
@RequestMapping("/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final AuthorRepository authorRepository;

    @GetMapping
    public ResponseEntity<List<Author>> list() {
        return ResponseEntity.ok(authorRepository.findAll());
    }

    /**
     * 创建作者：若同名作者已存在，则直接返回已存在的记录
     */
    @PostMapping
    public ResponseEntity<Author> create(@RequestBody CreateAuthorRequest req) {
        String name = req.getName() != null ? req.getName().trim() : "";
        if (name.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Author existing = authorRepository.findByName(name);
        if (existing != null) {
            return ResponseEntity.ok(existing);
        }
        Author author = new Author();
        author.setName(name);
        Author saved = authorRepository.save(author);
        return ResponseEntity.ok(saved);
    }

    @Data
    public static class CreateAuthorRequest {
        private String name;
    }
}

