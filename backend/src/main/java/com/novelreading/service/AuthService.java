package com.novelreading.service;

import com.novelreading.dto.AuthResponse;
import com.novelreading.dto.LoginRequest;
import com.novelreading.dto.RegisterRequest;
import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import com.novelreading.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户端认证服务（非管理员）
 * 实现注册、登录核心业务，并签发 JWT。
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final AuthenticationManager authenticationManager;

    /**
     * 注册流程：
     * 1) 校验必要字段与唯一性；2) 写入用户（密码加密）；
     * 3) 注册成功后直接签发 token，前端可免二次登录。
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String username = request.getUsername() != null ? request.getUsername().trim() : "";
        String email = request.getEmail() != null ? request.getEmail().trim() : "";
        String password = request.getPassword() != null ? request.getPassword() : "";

        if (username.isEmpty()) {
            throw new RuntimeException("没有填写用户名");
        }
        if (email.isEmpty()) {
            throw new RuntimeException("邮件没有填写");
        }
        if (password.isEmpty()) {
            throw new RuntimeException("密码没有填写");
        }
        if (userRepository.existsByPk_Username(username)) {
            throw new RuntimeException("用户名已经被注册");
        }
        if (userRepository.existsByPk_Email(email)) {
            throw new RuntimeException("该邮箱已经被注册");
        }

        // 创建用户实体：密码必须存储为加密值，昵称为空时默认使用用户名。
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setNickname(request.getNickname() != null && !request.getNickname().isBlank()
                ? request.getNickname().trim() : username);
        user.setIsBanned(0);
        // 对于当前实体映射（复合主键 + 内部自增ID非主键），直接 refresh 可能在注册时触发
        // "No row with the given identifier exists"。这里改为先 flush 再按主键回查。
        userRepository.saveAndFlush(user);
        user = userRepository.findByPk_Username(username)
                .orElseThrow(() -> new RuntimeException("注册失败，请稍后重试"));

        String token = tokenProvider.generateToken(user.getUsername());
        AuthResponse resp = new AuthResponse();
        resp.setToken(token);
        resp.setUsername(user.getUsername());
        resp.setNickname(user.getNickname());
        resp.setUserId(user.getId());
        resp.setAvatarUrl(user.getAvatarUrl());
        return resp;
    }

    /**
     * 登录流程：
     * 1) 解析登录标识（用户名或邮箱）；2) 封禁状态判断；
     * 3) 委托 Spring Security 校验密码；4) 生成 JWT 并返回用户信息。
     */
    public AuthResponse login(LoginRequest request) {
        String loginId = request.getUsername().trim();
        String username = loginId;
        // 若输入包含 '@'，按邮箱登录并回查对应用户名。
        if (loginId.contains("@")) {
            username = userRepository.findFirstByPk_Email(loginId)
                    .map(User::getUsername)
                    .orElseThrow(() -> new BadCredentialsException("邮箱未注册"));
        } else {
            if (!userRepository.existsByPk_Username(loginId)) {
                throw new BadCredentialsException("用户不存在");
            }
        }

        User user = userRepository.findByPk_Username(username).orElseThrow(() -> new BadCredentialsException("用户不存在"));
        if (user.getIsBanned() != null && user.getIsBanned() == 1) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime bannedUntil = user.getBannedUntil();
            // 封禁到期则自动解禁；未到期则禁止登录。
            if (bannedUntil != null && now.isAfter(bannedUntil)) {
                user.setIsBanned(0);
                user.setBannedAt(null);
                user.setBannedUntil(null);
                userRepository.save(user);
            } else {
                throw new BadCredentialsException("该用户已经被封禁");
            }
        }

        try {
            // 使用 AuthenticationManager 做密码比对（内部会调用 UserDetailsService + PasswordEncoder）。
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, request.getPassword()));
            String token = tokenProvider.generateToken(authentication);
            user = userRepository.findByPk_Username(username).orElseThrow();
            AuthResponse resp = new AuthResponse();
            resp.setToken(token);
            resp.setUsername(user.getUsername());
            resp.setNickname(user.getNickname());
            resp.setUserId(user.getId());
            resp.setAvatarUrl(user.getAvatarUrl());
            return resp;
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("密码错误");
        }
    }
}
