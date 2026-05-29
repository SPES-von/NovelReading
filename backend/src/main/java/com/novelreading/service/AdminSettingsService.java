package com.novelreading.service;

import com.novelreading.dto.AdminChangePasswordRequest;
import com.novelreading.entity.Admin;
import com.novelreading.entity.NovelComment;
import com.novelreading.entity.ReadingProgress;
import com.novelreading.entity.User;
import com.novelreading.repository.AdminRepository;
import com.novelreading.repository.NovelCommentRepository;
import com.novelreading.repository.ReadingProgressRepository;
import com.novelreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统设置业务逻辑：
 * <p>
 * 「日志查看」并非读取应用日志文件，而是将用户注册、评论、阅读进度等表中最近记录
 * 合并为统一列表，按时间倒序截取 {@link #RECENT_LOG_LIMIT} 条，供管理端运营查看。
 */
@Service
@RequiredArgsConstructor
public class AdminSettingsService {

    /** 聚合日志返回的最大条数 */
    private static final int RECENT_LOG_LIMIT = 30;
    /** 与前台评论展示策略说明一致，供系统参数展示 */
    private static final String COMMENT_VISIBILITY_RULE = "仅展示已审核(audit=1)且踩击数<=10的评论";

    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final NovelCommentRepository novelCommentRepository;
    private final ReadingProgressRepository readingProgressRepository;

    /**
     * 组装系统设置页所需 JSON：systemParams、serverStatus、logs、generatedAt。
     */
    public Map<String, Object> getOverview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("systemParams", buildSystemParams());
        result.put("serverStatus", buildServerStatus());
        result.put("logs", buildRecentLogs());
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    /**
     * 校验原密码明文与库中一致后更新为新密码（当前实现为明文比对/存储，与现有 Admin 实体一致）。
     *
     * @return 成功 true；参数非法、管理员不存在或原密码错误 false
     */
    public boolean changeCurrentAdminPassword(Long adminId, AdminChangePasswordRequest request) {
        if (adminId == null || request == null) return false;
        String oldPassword = normalize(request.getOldPassword());
        String newPassword = normalize(request.getNewPassword());
        if (oldPassword == null || newPassword == null || newPassword.length() < 6) return false;

        Admin admin = adminRepository.findById(adminId).orElse(null);
        if (admin == null || !oldPassword.equals(admin.getPassword())) return false;
        admin.setPassword(newPassword);
        adminRepository.save(admin);
        return true;
    }

    /** 用户数、管理员数、封禁数、评论规则说明、维护模式占位等 */
    private Map<String, Object> buildSystemParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("adminCount", adminRepository.count());
        params.put("userCount", userRepository.countAllUsers());
        params.put("bannedUserCount", userRepository.countBannedUsers());
        params.put("commentVisibilityRule", COMMENT_VISIBILITY_RULE);
        params.put("maintenanceMode", false);
        return params;
    }

    /** 当前 JVM 与操作系统运行时信息（内存、CPU、启动时长等） */
    private Map<String, Object> buildServerStatus() {
        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        long max = runtime.maxMemory();
        long total = runtime.totalMemory();
        long free = runtime.freeMemory();
        long used = total - free;

        Map<String, Object> status = new LinkedHashMap<>();
        status.put("osName", System.getProperty("os.name"));
        status.put("osVersion", System.getProperty("os.version"));
        status.put("javaVersion", System.getProperty("java.version"));
        status.put("availableProcessors", runtime.availableProcessors());
        status.put("jvmUptimeSeconds", runtimeMXBean.getUptime() / 1000);
        status.put("jvmStartTime", LocalDateTime.now().minus(Duration.ofMillis(runtimeMXBean.getUptime())).toString());
        status.put("memoryUsedMB", bytesToMb(used));
        status.put("memoryTotalMB", bytesToMb(total));
        status.put("memoryMaxMB", bytesToMb(max));
        return status;
    }

    /**
     * 分别从 user / novel_comment / reading_progress 各取最近若干条，
     * 转为统一结构后按 time 降序排序，最多保留 {@link #RECENT_LOG_LIMIT} 条。
     */
    private List<Map<String, Object>> buildRecentLogs() {
        List<Map<String, Object>> logs = new ArrayList<>();
        int querySize = 12;

        List<User> users = userRepository.findByOrderByCreatedAtDesc(PageRequest.of(0, querySize));
        for (User user : users) {
            logs.add(log("USER_REGISTER", user.getCreatedAt(),
                    "用户注册: " + safe(user.getUsername()) + " (ID=" + user.getId() + ")", "user"));
        }

        List<NovelComment> comments = novelCommentRepository.findRecentWithUserAndNovel(PageRequest.of(0, querySize));
        for (NovelComment comment : comments) {
            String auditText = comment.getAudit() != null && comment.getAudit() == 1 ? "已审核" : "待审核";
            String title = comment.getNovel() != null ? safe(comment.getNovel().getTitle()) : "-";
            logs.add(log("COMMENT_CREATE", comment.getCreatedAt(),
                    "评论创建: [" + title + "] " + auditText + "，用户ID=" + (comment.getUser() != null ? comment.getUser().getId() : "-"), "novel_comment"));
        }

        List<ReadingProgress> progresses = readingProgressRepository.findRecentWithUserAndNovel(PageRequest.of(0, querySize));
        for (ReadingProgress rp : progresses) {
            String title = rp.getNovel() != null ? safe(rp.getNovel().getTitle()) : "-";
            logs.add(log("READ_PROGRESS", rp.getLastReadAt(),
                    "阅读进度更新: 用户ID=" + (rp.getUser() != null ? rp.getUser().getId() : "-") + "，小说=[" + title + "]", "reading_progress"));
        }

        logs.sort(Comparator.comparing(
                (Map<String, Object> x) -> String.valueOf(x.getOrDefault("time", ""))
        ).reversed());

        if (logs.size() > RECENT_LOG_LIMIT) {
            return new ArrayList<>(logs.subList(0, RECENT_LOG_LIMIT));
        }
        return logs;
    }

    /** 单条日志行：type、time、content、source，供前端表格列绑定 */
    private Map<String, Object> log(String type, LocalDateTime time, String content, String source) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("type", type);
        row.put("time", time != null ? time.toString() : null);
        row.put("content", content);
        row.put("source", source);
        return row;
    }

    private static long bytesToMb(long bytes) {
        if (bytes <= 0) return 0;
        return Math.round(bytes / 1024.0 / 1024.0);
    }

    private static String normalize(String text) {
        if (text == null) return null;
        String t = text.trim();
        return t.isEmpty() ? null : t;
    }

    private static String safe(String text) {
        return text == null ? "" : text;
    }
}
