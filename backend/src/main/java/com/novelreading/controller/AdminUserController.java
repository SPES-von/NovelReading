package com.novelreading.controller;

import com.novelreading.dto.AdminUserBanRequest;
import com.novelreading.dto.AdminUserDTO;
import com.novelreading.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员用户管理接口：列表、删除、封禁、解禁
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    /** 用户列表，支持 keyword（用户ID/用户名/邮箱模糊）、year/month/day 注册日期筛选 */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer day) {
        return ResponseEntity.ok(adminUserService.listAll(keyword, year, month, day));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        boolean ok = adminUserService.deleteById(id);
        if (!ok) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/ban")
    public ResponseEntity<Map<String, Object>> ban(@PathVariable Long id, @RequestBody AdminUserBanRequest request) {
        boolean ok = adminUserService.banUser(id, request);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "封禁失败"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/{id}/unban")
    public ResponseEntity<Map<String, Object>> unban(@PathVariable Long id) {
        boolean ok = adminUserService.unbanUser(id);
        if (!ok) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "解禁失败"));
        }
        return ResponseEntity.ok(Map.of("success", true));
    }
}
