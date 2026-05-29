package com.novelreading.controller;

import com.novelreading.dto.AuthResponse;
import com.novelreading.dto.LoginRequest;
import com.novelreading.dto.RegisterRequest;
import com.novelreading.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 用户端认证控制器（非管理员）
 * 负责暴露登录、注册接口，并将业务异常转换为前端可直接展示的提示文案。
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录接口：
     * 支持“用户名/邮箱 + 密码”登录，成功后返回 JWT 和用户基础信息。
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    /**
     * 用户注册接口：
     * 完成基础校验、创建用户，并直接返回登录态（JWT）。
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // 登录时用户名不存在等场景，统一返回 401。
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleUserNotFound(UsernameNotFoundException e) {
        return ResponseEntity.status(401).body(Map.of("message", "用户不存在"));
    }

    // 登录失败（如密码错误、邮箱未注册、被封禁）统一走这里。
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(401).body(Map.of("message", e.getMessage() != null ? e.getMessage() : "用户不存在"));
    }

    // 注册参数校验失败时返回 400，其他运行时异常继续抛出给全局处理器。
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRegisterError(RuntimeException e) {
        String msg = e.getMessage();
        if (msg != null && (msg.equals("没有填写用户名") || msg.equals("邮件没有填写") || msg.equals("密码没有填写")
                || msg.equals("用户名已经被注册") || msg.equals("该邮箱已经被注册"))) {
            return ResponseEntity.badRequest().body(Map.of("message", msg));
        }
        throw e;
    }
}
