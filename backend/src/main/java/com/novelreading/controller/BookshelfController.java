package com.novelreading.controller;

import com.novelreading.dto.NovelDTO;
import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import com.novelreading.service.NovelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 我的书架接口
 * 需登录，返回当前用户书架中的小说列表
 */
@RestController
@RequestMapping("/bookshelf")
@RequiredArgsConstructor
public class BookshelfController {

    private final NovelService novelService;
    private final UserRepository userRepository;

    /** 获取当前用户书架中的小说列表 */
    @GetMapping
    public ResponseEntity<List<NovelDTO>> getMyBookshelf() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByPk_Username(auth.getName()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<NovelDTO> list = novelService.getBookshelf(user.getId());
        return ResponseEntity.ok(list);
    }

    /** 从书架移除指定小说 */
    @DeleteMapping("/{novelId}")
    public ResponseEntity<Object> removeFromBookshelf(@PathVariable Long novelId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(401).build();
        }
        User user = userRepository.findByPk_Username(auth.getName()).orElse(null);
        if (user == null) return ResponseEntity.status(401).build();
        boolean removed = novelService.removeFromBookshelf(user.getId(), novelId);
        return ResponseEntity.ok(java.util.Map.of("removed", removed));
    }
}
