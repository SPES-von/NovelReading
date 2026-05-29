package com.novelreading.controller;

import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import com.novelreading.service.ReadingProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 阅读进度/书签接口
 * 需登录：保存/查询当前阅读位置（书签）到 reading_progress 表，同用户同书则更新
 */
@RestController
@RequestMapping("/reading-progress")
@RequiredArgsConstructor
public class ReadingProgressController {

    private final ReadingProgressService readingProgressService;
    private final UserRepository userRepository;

    /**
     * 获取当前用户对某本书的阅读进度（书签），用于详情页展示「当前阅读到：章名」及开始阅读跳转
     * GET /reading-progress?novelId=xxx ，返回 { chapterId, chapterTitle } 或 null
     */
    @GetMapping
    public ResponseEntity<?> getProgress(@RequestParam Long novelId) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByPk_Username(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return readingProgressService.getProgressForNovel(user.getId(), novelId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    /**
     * 获取当前用户全部阅读历史（reading_progress 表该用户所有记录），按最后阅读时间倒序
     * GET /reading-progress/list ，返回 [{ novelId, title, coverUrl, chapterId, chapterTitle, bookmark }, ...]
     */
    @GetMapping("/list")
    public ResponseEntity<?> listAll() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByPk_Username(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<Map<String, Object>> list = readingProgressService.listAllByUser(user.getId());
        return ResponseEntity.ok(list);
    }

    /**
     * 保存书签：当前阅读的书籍、章节及阅读位置写入/更新 reading_progress
     * 请求体：{ "novelId": number, "chapterId": number, "progressPosition": number?, "bookmark": number? }
     * 若已存在同用户同书则更新 chapter_id、progress_position、bookmark（含不同卷/章）
     */
    @PostMapping("/bookmark")
    public ResponseEntity<?> saveBookmark(@RequestBody Map<String, Object> body) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByPk_Username(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        if (body == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "缺少请求体"));
        }
        Long novelId = numberFrom(body.get("novelId"));
        Long chapterId = numberFrom(body.get("chapterId"));
        if (novelId == null || chapterId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "缺少 novelId 或 chapterId"));
        }
        Integer progressPosition = integerFrom(body.get("progressPosition"));
        Integer bookmark = integerFrom(body.get("bookmark"));
        try {
            readingProgressService.saveOrUpdateBookmark(user.getId(), novelId, chapterId, progressPosition, bookmark);
            return ResponseEntity.ok(Map.of("message", "书签已保存"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private static Long numberFrom(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        if (o instanceof String) {
            try { return Long.parseLong((String) o); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }

    private static Integer integerFrom(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).intValue();
        if (o instanceof String) {
            try { return Integer.parseInt((String) o); } catch (NumberFormatException e) { return null; }
        }
        return null;
    }
}
