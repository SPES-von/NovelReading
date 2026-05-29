package com.novelreading.controller;

import com.novelreading.dto.NovelCommentDTO;
import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import com.novelreading.service.NovelCommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小说评论接口
 * GET 某本书的评论（公开）；POST 发布评论（需登录）
 */
@RestController
@RequestMapping("/novels")
@RequiredArgsConstructor
public class NovelCommentController {

    private final NovelCommentService novelCommentService;
    private final UserRepository userRepository;

    /** 获取某本书的评论列表：踩击数≤10 才展示，按点赞数降序 */
    @GetMapping("/{novelId}/comments")
    public ResponseEntity<List<NovelCommentDTO>> list(@PathVariable Long novelId) {
        return ResponseEntity.ok(novelCommentService.listByNovelId(novelId));
    }

    /** 发布评论：需登录，请求体 { "content": "评论内容" } */
    @PostMapping("/{novelId}/comments")
    public ResponseEntity<?> create(@PathVariable Long novelId, @RequestBody Map<String, Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByPk_Username(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        String content = body != null && body.get("content") != null ? body.get("content").toString() : null;
        try {
            NovelCommentDTO dto = novelCommentService.create(user.getId(), novelId, content);
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 点赞：评论 like_count +1 */
    @PostMapping("/{novelId}/comments/{commentId}/like")
    public ResponseEntity<?> like(@PathVariable Long novelId, @PathVariable Long commentId) {
        try {
            novelCommentService.incrementLike(novelId, commentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** 踩：评论 dislike_count +1 */
    @PostMapping("/{novelId}/comments/{commentId}/dislike")
    public ResponseEntity<?> dislike(@PathVariable Long novelId, @PathVariable Long commentId) {
        try {
            novelCommentService.incrementDislike(novelId, commentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
