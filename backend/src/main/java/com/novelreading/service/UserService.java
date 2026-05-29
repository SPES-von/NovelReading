package com.novelreading.service;

import com.novelreading.dto.UserProfileDTO;
import com.novelreading.dto.UserProfileUpdateRequest;
import com.novelreading.dto.UserReadPreferencesDTO;
import com.novelreading.entity.User;
import com.novelreading.entity.UserReadPreferences;
import com.novelreading.repository.UserReadPreferencesRepository;
import com.novelreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 用户服务：个人信息与阅读偏好
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserReadPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileDTO getProfile(Long userId) {
        User user = userRepository.findByInternalId(userId).orElse(null);
        if (user == null) return null;
        UserProfileDTO dto = new UserProfileDTO();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setNickname(user.getNickname());
        dto.setEmail(user.getEmail());
        dto.setAvatarUrl(user.getAvatarUrl());
        return dto;
    }

    @Transactional
    public UserProfileDTO updateProfile(Long userId, UserProfileUpdateRequest req) {
        User user = userRepository.findByInternalId(userId).orElse(null);
        if (user == null) return null;

        if (req.getNickname() != null) {
            user.setNickname(req.getNickname().trim().isEmpty() ? user.getUsername() : req.getNickname().trim());
        }
        // 用户名、邮箱为复合主键，不允许在资料中修改
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
                throw new RuntimeException("密码相同");
            }
            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        }

        user = userRepository.save(user);
        return getProfile(user.getId());
    }

    public UserReadPreferencesDTO getPreferences(Long userId) {
        UserReadPreferences prefs = preferencesRepository.findByUser_InternalId(userId).orElse(null);
        if (prefs == null) {
            UserReadPreferencesDTO dto = new UserReadPreferencesDTO();
            return dto;
        }
        UserReadPreferencesDTO dto = new UserReadPreferencesDTO();
        dto.setFontSize(prefs.getFontSize());
        dto.setLineHeight(prefs.getLineHeight());
        dto.setTheme(prefs.getTheme());
        dto.setPageWidth(prefs.getPageWidth());
        return dto;
    }

    @Transactional
    public UserReadPreferencesDTO updatePreferences(Long userId, UserReadPreferencesDTO req) {
        UserReadPreferences prefs = preferencesRepository.findByUser_InternalId(userId).orElse(null);
        if (prefs == null) {
            User user = userRepository.findByInternalId(userId).orElse(null);
            if (user == null) return null;
            prefs = new UserReadPreferences();
            prefs.setUser(user);
        }
        if (req.getFontSize() != null && req.getFontSize() >= 12 && req.getFontSize() <= 28) {
            prefs.setFontSize(req.getFontSize());
        }
        if (req.getLineHeight() != null && req.getLineHeight().doubleValue() >= 1.2 && req.getLineHeight().doubleValue() <= 3.0) {
            prefs.setLineHeight(req.getLineHeight());
        }
        if (req.getTheme() != null && !req.getTheme().isBlank()) {
            String t = req.getTheme().trim();
            if (t.matches("default|sepia|night")) {
                prefs.setTheme(t);
            }
        }
        if (req.getPageWidth() != null && req.getPageWidth() >= 0 && req.getPageWidth() <= 1200) {
            prefs.setPageWidth(req.getPageWidth());
        }
        preferencesRepository.save(prefs);
        return getPreferences(userId);
    }
}
