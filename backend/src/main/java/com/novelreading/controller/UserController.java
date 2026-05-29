package com.novelreading.controller;

import com.novelreading.dto.UserProfileDTO;
import com.novelreading.dto.UserProfileUpdateRequest;
import com.novelreading.dto.UserReadPreferencesDTO;
import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import com.novelreading.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 用户个人中心控制器
 * 账户信息、阅读偏好，需登录访问
 */
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        return userRepository.findByPk_Username(auth.getName()).orElse(null);
    }

    /** 获取当前用户个人信息 */
    @GetMapping("/profile")
    public ResponseEntity<UserProfileDTO> getProfile() {
        User user = currentUser();
        if (user == null) return ResponseEntity.status(401).build();
        UserProfileDTO dto = userService.getProfile(user.getId());
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }

    /** 更新当前用户个人信息（用户名、昵称、邮箱、密码） */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileUpdateRequest request) {
        User user = currentUser();
        if (user == null) return ResponseEntity.status(401).build();
        try {
            UserProfileDTO dto = userService.updateProfile(user.getId(), request);
            return ResponseEntity.ok(dto);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }

    /** 获取当前用户阅读偏好 */
    @GetMapping("/preferences")
    public ResponseEntity<UserReadPreferencesDTO> getPreferences() {
        User user = currentUser();
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(userService.getPreferences(user.getId()));
    }

    /** 更新当前用户阅读偏好 */
    @PutMapping("/preferences")
    public ResponseEntity<UserReadPreferencesDTO> updatePreferences(@RequestBody UserReadPreferencesDTO request) {
        User user = currentUser();
        if (user == null) return ResponseEntity.status(401).build();
        UserReadPreferencesDTO dto = userService.updatePreferences(user.getId(), request);
        return dto != null ? ResponseEntity.ok(dto) : ResponseEntity.notFound().build();
    }
}
