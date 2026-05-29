package com.novelreading.service;

import com.novelreading.dto.AdminAuthResponse;
import com.novelreading.dto.AdminLoginRequest;
import com.novelreading.entity.Admin;
import com.novelreading.repository.AdminRepository;
import com.novelreading.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

/**
 * 管理员认证服务
 * 明文校验 id/密码，生成 JWT（subject 为 "admin:{id}" 以区分普通用户）
 */
@Service
@RequiredArgsConstructor
public class AdminAuthService {

    public static final String ADMIN_SUBJECT_PREFIX = "admin:";

    private final AdminRepository adminRepository;
    private final JwtTokenProvider tokenProvider;

    /**
     * 登录鉴权主流程：
     * 1) 校验 adminId 非空
     * 2) 查询管理员是否存在
     * 3) 校验密码
     * 4) 生成管理员 JWT 并返回
     */
    public AdminAuthResponse login(AdminLoginRequest request) {
        if (request.getAdminId() == null) {
            throw new BadCredentialsException("请输入ID");
        }
        Admin admin = adminRepository.findById(request.getAdminId())
                .orElseThrow(() -> new BadCredentialsException("不存在该管理员"));
        if (request.getPassword() == null || !request.getPassword().equals(admin.getPassword())) {
            throw new BadCredentialsException("密码错误请重新输入");
        }
        // 在 subject 前添加 admin: 前缀，便于过滤器区分管理员令牌与普通用户令牌
        String subject = ADMIN_SUBJECT_PREFIX + admin.getId();
        String token = tokenProvider.generateToken(subject);
        AdminAuthResponse resp = new AdminAuthResponse();
        resp.setToken(token);
        resp.setAdminId(admin.getId());
        return resp;
    }
}
