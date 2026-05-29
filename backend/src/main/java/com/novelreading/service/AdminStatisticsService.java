package com.novelreading.service;

import com.novelreading.repository.NovelCommentRepository;
import com.novelreading.repository.NovelRepository;
import com.novelreading.repository.ReadingProgressRepository;
import com.novelreading.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminStatisticsService {

    private final UserRepository userRepository;
    private final NovelRepository novelRepository;
    private final ReadingProgressRepository readingProgressRepository;
    private final NovelCommentRepository novelCommentRepository;

    public Map<String, Object> buildOverview() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime sevenDayStart = LocalDate.now().minusDays(6).atStartOfDay();

        long totalUsers = userRepository.countAllUsers();
        long totalNovels = novelRepository.count();
        long totalReadEvents = readingProgressRepository.countAllReadEvents();
        long totalComments = novelCommentRepository.countAllComments();

        long todayReadEvents = readingProgressRepository.countReadEventsSince(todayStart);
        long sevenDayReadEvents = readingProgressRepository.countReadEventsSince(sevenDayStart);
        long totalReadingUsers = readingProgressRepository.countDistinctReadingUsers();

        long todayActiveUsers = readingProgressRepository.countDistinctActiveUsersSince(todayStart);
        long sevenDayActiveUsers = readingProgressRepository.countDistinctActiveUsersSince(sevenDayStart);
        long todayNewUsers = userRepository.countCreatedSince(todayStart);
        long sevenDayNewUsers = userRepository.countCreatedSince(sevenDayStart);
        long sevenDayNewComments = novelCommentRepository.countCommentsSince(sevenDayStart);

        List<Map<String, Object>> hotNovels = buildHotNovels();
        List<Map<String, Object>> readTrend = buildReadTrend();

        Map<String, Object> core = new LinkedHashMap<>();
        core.put("totalUsers", totalUsers);
        core.put("totalNovels", totalNovels);
        core.put("totalReadEvents", totalReadEvents);
        core.put("totalComments", totalComments);

        Map<String, Object> reading = new LinkedHashMap<>();
        reading.put("todayReadEvents", todayReadEvents);
        reading.put("sevenDayReadEvents", sevenDayReadEvents);
        reading.put("totalReadingUsers", totalReadingUsers);

        Map<String, Object> active = new LinkedHashMap<>();
        active.put("todayActiveUsers", todayActiveUsers);
        active.put("sevenDayActiveUsers", sevenDayActiveUsers);
        active.put("todayNewUsers", todayNewUsers);
        active.put("sevenDayNewUsers", sevenDayNewUsers);
        active.put("sevenDayNewComments", sevenDayNewComments);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("core", core);
        result.put("reading", reading);
        result.put("active", active);
        result.put("hotNovels", hotNovels);
        result.put("readTrend", readTrend);
        result.put("generatedAt", LocalDateTime.now().toString());
        return result;
    }

    public byte[] exportCsv() {
        Map<String, Object> overview = buildOverview();
        @SuppressWarnings("unchecked")
        Map<String, Object> core = (Map<String, Object>) overview.get("core");
        @SuppressWarnings("unchecked")
        Map<String, Object> reading = (Map<String, Object>) overview.get("reading");
        @SuppressWarnings("unchecked")
        Map<String, Object> active = (Map<String, Object>) overview.get("active");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> hotNovels = (List<Map<String, Object>>) overview.get("hotNovels");

        StringBuilder sb = new StringBuilder();
        sb.append("运营分析报表\n");
        sb.append("生成时间,").append(csvEscape(String.valueOf(overview.get("generatedAt")))).append("\n\n");

        sb.append("核心概览\n");
        sb.append("总用户数,总小说数,累计阅读事件,累计评论数\n");
        sb.append(core.get("totalUsers")).append(',')
                .append(core.get("totalNovels")).append(',')
                .append(core.get("totalReadEvents")).append(',')
                .append(core.get("totalComments")).append("\n\n");

        sb.append("阅读统计\n");
        sb.append("今日阅读事件,近7日阅读事件,累计有阅读行为用户数\n");
        sb.append(reading.get("todayReadEvents")).append(',')
                .append(reading.get("sevenDayReadEvents")).append(',')
                .append(reading.get("totalReadingUsers")).append("\n\n");

        sb.append("用户活跃\n");
        sb.append("今日活跃用户,近7日活跃用户,今日新增用户,近7日新增用户,近7日新增评论\n");
        sb.append(active.get("todayActiveUsers")).append(',')
                .append(active.get("sevenDayActiveUsers")).append(',')
                .append(active.get("todayNewUsers")).append(',')
                .append(active.get("sevenDayNewUsers")).append(',')
                .append(active.get("sevenDayNewComments")).append("\n\n");

        sb.append("小说热度TOP10\n");
        sb.append("排名,小说ID,小说名称,阅读量,阅读用户数,热度分\n");
        for (int i = 0; i < hotNovels.size(); i++) {
            Map<String, Object> row = hotNovels.get(i);
            sb.append(i + 1).append(',')
                    .append(row.get("novelId")).append(',')
                    .append(csvEscape(String.valueOf(row.get("title")))).append(',')
                    .append(row.get("viewCount")).append(',')
                    .append(row.get("readingUsers")).append(',')
                    .append(row.get("heatScore")).append('\n');
        }

        String csv = "\uFEFF" + sb;
        return csv.getBytes(StandardCharsets.UTF_8);
    }

    private List<Map<String, Object>> buildHotNovels() {
        List<Object[]> rows = readingProgressRepository.findTop10NovelHeatStats();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Long novelId = asLong(row[0]);
            String title = row[1] != null ? row[1].toString() : "";
            long viewCount = asLong(row[2]);
            long readingUsers = asLong(row[3]);
            long heatScore = Math.round(viewCount * 0.6 + readingUsers * 40.0);

            Map<String, Object> item = new LinkedHashMap<>();
            item.put("novelId", novelId);
            item.put("title", title);
            item.put("viewCount", viewCount);
            item.put("readingUsers", readingUsers);
            item.put("heatScore", heatScore);
            result.add(item);
        }
        return result;
    }

    private List<Map<String, Object>> buildReadTrend() {
        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        Map<String, Long> countMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            countMap.put(day.format(dateFmt), 0L);
        }
        for (Object[] row : readingProgressRepository.findRecent7DayReadTrend()) {
            String day = String.valueOf(row[0]);
            long count = asLong(row[1]);
            countMap.put(day, count);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        countMap.forEach((date, count) -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", date);
            item.put("count", count);
            result.add(item);
        });
        return result;
    }

    private static long asLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Number n) return n.longValue();
        return Long.parseLong(val.toString());
    }

    private static String csvEscape(String value) {
        if (value == null) return "";
        String escaped = value.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
