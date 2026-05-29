package com.novelreading.controller;

import com.novelreading.dto.AdminCommentDTO;
import com.novelreading.service.NovelCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理端评论管理：列表（novel_comment 全部）、审核、删除
 */
@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
public class AdminCommentController {

    private final NovelCommentService novelCommentService;

    /** 获取全部评论，按评论时间倒序；支持 keyword（评论内容/用户ID 模糊）、year/month/day 日期筛选 */
    @GetMapping
    public ResponseEntity<List<AdminCommentDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
        return ResponseEntity.ok(novelCommentService.listAllForAdmin(keyword, year, month, day));
    }

    /** 审核通过：将 audit 设为 1 */
    @PutMapping("/{id}/audit")
    public ResponseEntity<Map<String, Object>> audit(@PathVariable Long id) {
        boolean ok = novelCommentService.audit(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 删除评论 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean ok = novelCommentService.deleteById(id);
        if (!ok) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(Map.of("success", true));
    }
}
