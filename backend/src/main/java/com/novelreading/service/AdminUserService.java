package com.novelreading.service;

import com.novelreading.dto.AdminUserBanRequest;
import com.novelreading.dto.AdminUserDTO;
import com.novelreading.entity.User;
import com.novelreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员用户管理服务：列表、删除、封禁、解禁
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;

    /** 管理端：全部用户或按关键词（用户ID/用户名/邮箱模糊）、注册日期筛选 */
    public List<AdminUserDTO> listAll(String keyword, Integer year, Integer month, Integer day) {
        String dateStr = null;
        if (year != null && month != null && day != null) {
            dateStr = LocalDate.of(year, month, day).toString();
        }
        boolean hasFilter = (keyword != null && !keyword.isBlank()) || (dateStr != null && !dateStr.isEmpty());
        if (!hasFilter) {
            return userRepository.findAll().stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        }
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        List<Long> ids = userRepository.findUserIdsForAdminSearch(kw, dateStr);
        if (ids == null || ids.isEmpty()) return Collections.emptyList();
        return userRepository.findByInternalIdInOrderByCreatedAtDesc(ids).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean deleteById(Long id) {
        return userRepository.findByInternalId(id)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean banUser(Long id, AdminUserBanRequest request) {
        if (request == null || request.getDurationHours() == null || request.getDurationHours() <= 0) {
            return false;
        }
        return userRepository.findByInternalId(id)
                .map(user -> {
                    LocalDateTime now = LocalDateTime.now();
                    user.setIsBanned(1);
                    user.setBannedAt(now);
                    user.setBannedUntil(now.plusHours(request.getDurationHours()));
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    @Transactional
    public boolean unbanUser(Long id) {
        return userRepository.findByInternalId(id)
                .map(user -> {
                    user.setIsBanned(0);
                    user.setBannedAt(null);
                    user.setBannedUntil(null);
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    private AdminUserDTO toDTO(User u) {
        AdminUserDTO dto = new AdminUserDTO();
        dto.setId(u.getId());
        dto.setUsername(u.getUsername());
        dto.setEmail(u.getEmail());
        dto.setCreatedAt(u.getCreatedAt());
        dto.setIsBanned(u.getIsBanned() != null ? u.getIsBanned() : 0);
        dto.setBannedAt(u.getBannedAt());
        dto.setBannedUntil(u.getBannedUntil());
        return dto;
    }
}
