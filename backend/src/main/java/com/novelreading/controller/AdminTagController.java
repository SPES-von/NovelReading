package com.novelreading.controller;

import com.novelreading.entity.Tag;
import com.novelreading.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员标签管理：列表、添加、删除
 */
@RestController
@RequestMapping("/admin/tags")
@RequiredArgsConstructor
public class AdminTagController {

    private final TagRepository tagRepository;

    @GetMapping
    public ResponseEntity<List<Tag>> list() {
        return ResponseEntity.ok(tagRepository.findAllByOrderByIdAsc());
    }

    @PostMapping
    public ResponseEntity<?> add(@RequestBody Map<String, String> body) {
        String name = body != null ? body.get("name") : null;
        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "标签名称不能为空"));
        }
        name = name.trim();
        if (tagRepository.existsByName(name)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "该标签已存在"));
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag = tagRepository.save(tag);
        return ResponseEntity.status(HttpStatus.CREATED).body(tag);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        if (!tagRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        tagRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
