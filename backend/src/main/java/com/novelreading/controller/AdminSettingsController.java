package com.novelreading.controller;

import com.novelreading.dto.AdminChangePasswordRequest;
import com.novelreading.service.AdminAuthService;
import com.novelreading.service.AdminSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 管理端「系统设置」接口：
 * <ul>
 *   <li>GET /overview — 系统参数、JVM/OS 状态、聚合业务日志</li>
 *   <li>PUT /password — 当前登录管理员修改密码（需 Security 上下文中的 admin 主体）</li>
 * </ul>
 */
@RestController
@RequestMapping("/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final AdminSettingsService adminSettingsService;

    /**
     * 系统设置页一次性所需数据，供前端表格展示。
     */
    @GetMapping("/overview")
    public ResponseEntity<Map<String, Object>> overview() {
        return ResponseEntity.ok(adminSettingsService.getOverview());
    }

    /**
     * 修改当前管理员密码；principal 需为 {@link AdminAuthService#ADMIN_SUBJECT_PREFIX} + id。
     */
    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody AdminChangePasswordRequest request) {
        Long currentAdminId = currentAdminId();
        boolean ok = adminSettingsService.changeCurrentAdminPassword(currentAdminId, request);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "密码修改失败，请检查原密码和新密码长度"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    /** 从 Spring Security 解析当前管理员 ID；格式不符则返回 null。 */
    private Long currentAdminId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getPrincipal() == null) return null;
        String principal = String.valueOf(auth.getPrincipal());
        if (!principal.startsWith(AdminAuthService.ADMIN_SUBJECT_PREFIX)) return null;
        String idPart = principal.substring(AdminAuthService.ADMIN_SUBJECT_PREFIX.length());
        try {
            return Long.valueOf(idPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
