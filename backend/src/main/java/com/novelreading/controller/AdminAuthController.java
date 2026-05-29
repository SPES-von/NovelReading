package com.novelreading.controller;

import com.novelreading.dto.AdminAuthResponse;
import com.novelreading.dto.AdminLoginRequest;
import com.novelreading.service.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理员认证控制器
 * 提供管理员登录接口
 * 调用链：前端 AdminLogin.vue -> POST /admin/auth/login -> AdminAuthService -> 返回 JWT
 */
@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    /**
     * 管理员登录入口
     * 请求体：{ adminId, password }
     * 成功：返回 { token, adminId }，前端据此保存 adminToken 并进入后台
     */
    @PostMapping("/login")
    public ResponseEntity<AdminAuthResponse> login(@RequestBody AdminLoginRequest request) {
        return ResponseEntity.ok(adminAuthService.login(request));
    }

    /** 将凭证错误统一映射为 401，便于前端直接展示错误文案 */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        String msg = e.getMessage() != null ? e.getMessage() : "登录失败";
        return ResponseEntity.status(401).body(Map.of("message", msg));
    }
}
