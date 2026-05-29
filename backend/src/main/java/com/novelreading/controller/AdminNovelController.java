package com.novelreading.controller;

import com.novelreading.entity.Author;
import com.novelreading.entity.Chapter;
import com.novelreading.entity.Novel;
import com.novelreading.entity.Publisher;
import com.novelreading.repository.AuthorRepository;
import com.novelreading.repository.ChapterRepository;
import com.novelreading.repository.NovelRepository;
import com.novelreading.repository.PublisherRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理员小说管理：列表、创建等
 */
@RestController
@RequestMapping("/admin/novels")
@RequiredArgsConstructor
public class AdminNovelController {

    private final NovelRepository novelRepository;
    private final AuthorRepository authorRepository;
    private final PublisherRepository publisherRepository;
    private final ChapterRepository chapterRepository;

    @GetMapping
    public ResponseEntity<List<NovelIdTitleDTO>> list() {
        List<NovelIdTitleDTO> list = novelRepository.findAll().stream()
                .map(n -> new NovelIdTitleDTO(n.getId(), n.getTitle()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    /**
     * 创建新小说，并写入首章到 chapter 表
     */
    @PostMapping
    public ResponseEntity<?> create(@RequestBody CreateNovelRequest req) {
        if (req.getTitle() == null || req.getTitle().isBlank()) {
            return ResponseEntity.badRequest().body("title is required");
        }
        if (req.getAuthorId() == null || req.getPublisherId() == null) {
            return ResponseEntity.badRequest().body("authorId and publisherId are required");
        }
        Author author = authorRepository.findById(req.getAuthorId()).orElse(null);
        Publisher publisher = publisherRepository.findById(req.getPublisherId()).orElse(null);
        if (author == null || publisher == null) {
            return ResponseEntity.badRequest().body("author or publisher not found");
        }

        Novel novel = new Novel();
        novel.setTitle(req.getTitle());
        novel.setAuthor(author);
        novel.setPublisher(publisher);
        novel.setCoverUrl(req.getCoverUrl());
        novel.setSynopsis(req.getSynopsis());
        novel.setWordCount(req.getWordCount() != null ? req.getWordCount() : 0);

        Novel saved = novelRepository.save(novel);

        // 处理章节插入（上架页复用）
        String volume = (req.getVolume() == null || req.getVolume().isBlank()) ? "1" : req.getVolume().trim();
        String chapterTitle = (req.getChapterTitle() == null || req.getChapterTitle().isBlank())
                ? "第一章" : req.getChapterTitle().trim();
        String rawContent = req.getChapterContent() != null ? req.getChapterContent() : "";

        String htmlContent = buildChapterHtml(rawContent);
        int chapterWordCount = req.getWordCount() != null ? req.getWordCount() : countNonWhitespaceChars(rawContent);

        Chapter savedChapter = insertChapterWithSort(saved, volume, chapterTitle, htmlContent, chapterWordCount);

        // 更新小说最新章节信息
        saved.setLatestChapterId(savedChapter.getId());
        saved.setLatestChapterTitle(savedChapter.getTitle());
        novelRepository.save(saved);

        return ResponseEntity.ok(new NovelIdTitleDTO(saved.getId(), saved.getTitle()));
    }

    public record NovelIdTitleDTO(Long id, String title) {}

    @Data
    public static class CreateNovelRequest {
        private String title;
        private String synopsis;
        private Long authorId;
        private Long publisherId;
        private String coverUrl;
        private Integer wordCount;
        private String volume;
        private String chapterTitle;
        private String chapterContent;
    }

    /**
     * 管理端：为已存在的小说新增章节（支持卷不存在时插入新卷的首章）
     *
     * 规则：
     * - 卷名不存在：sort_order=0；sort（volumeSort）=当前书下最大 sort + 1
     * - 卷名存在：sort_order=当前卷 max+1；sort（volumeSort）保持该卷的 sort
     * - 正文：每行包裹 <p class="read-dialogue"></p>；空行 -> <br>；图片路径行 -> <img ... class="read-content-img" />
     */
    @PostMapping("/{novelId}/chapters")
    public ResponseEntity<?> addChapter(@PathVariable Long novelId, @RequestBody UpsertChapterRequest req) {
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) return ResponseEntity.badRequest().body("novel not found");

        String volume = (req.getVolume() == null || req.getVolume().isBlank()) ? "1" : req.getVolume().trim();
        String chapterTitle = (req.getTitle() == null || req.getTitle().isBlank()) ? "未命名章节" : req.getTitle().trim();
        String rawContent = req.getContent() != null ? req.getContent() : "";

        String htmlContent = buildChapterHtml(rawContent);
        int wordCount = req.getWordCount() != null ? req.getWordCount() : countNonWhitespaceChars(rawContent);

        Chapter savedChapter = insertChapterWithSort(novel, volume, chapterTitle, htmlContent, wordCount);

        novel.setLatestChapterId(savedChapter.getId());
        novel.setLatestChapterTitle(savedChapter.getTitle());
        novelRepository.save(novel);

        return ResponseEntity.ok(savedChapter);
    }

    /**
     * 管理端：更新章节内容（卷名/章节名/内容可改）
     * - 若调整了卷名：按“卷是否存在”重新计算 sort/sort_order（保持同 create 规则）
     */
    @PutMapping("/chapters/{chapterId}")
    @Transactional
    public ResponseEntity<?> updateChapter(@PathVariable Long chapterId, @RequestBody UpsertChapterRequest req) {
        Chapter existing = chapterRepository.findById(chapterId).orElse(null);
        if (existing == null) return ResponseEntity.badRequest().body("chapter not found");

        // 注意：Chapter.novel 是 LAZY，直接访问 novel 的非 id 字段可能触发 LazyInitializationException。
        // 这里用 novelId 重新加载 Novel，避免在无 Session 时访问代理字段。
        Long novelId = existing.getNovel() != null ? existing.getNovel().getId() : null;
        if (novelId == null) return ResponseEntity.badRequest().body("novel not found");
        Novel novel = novelRepository.findById(novelId).orElse(null);
        if (novel == null) return ResponseEntity.badRequest().body("novel not found");

        Long oldLatestChapterId = novel.getLatestChapterId();
        boolean shouldUpdateLatest = oldLatestChapterId != null && oldLatestChapterId.equals(chapterId);

        String volume = (req.getVolume() == null || req.getVolume().isBlank()) ? existing.getVolume() : req.getVolume().trim();
        String chapterTitle = (req.getTitle() == null || req.getTitle().isBlank()) ? existing.getTitle() : req.getTitle().trim();
        String rawContent = req.getContent() != null ? req.getContent() : "";

        String htmlContent = buildChapterHtml(rawContent);
        int wordCount = req.getWordCount() != null ? req.getWordCount() : countNonWhitespaceChars(rawContent);

        // 先删掉原有章节记录，再按新内容插入
        // 规则：卷和章都存在的更新场景下，sort_order 与 sort（volumeSort）不发生改变
        Integer oldVolumeSort = existing.getVolumeSort();
        Integer oldSortOrder = existing.getSortOrder();

        chapterRepository.deleteById(chapterId);

        Chapter chapter = new Chapter();
        chapter.setNovel(novel);
        chapter.setVolume(volume);
        chapter.setTitle(chapterTitle);
        chapter.setContent(htmlContent);
        chapter.setWordCount(wordCount);
        chapter.setVolumeSort(oldVolumeSort != null ? oldVolumeSort : 1);
        chapter.setSortOrder(oldSortOrder != null ? oldSortOrder : 0);
        Chapter saved = chapterRepository.save(chapter);

        // 若更新的是最新章节，保持小说 latest 字段一致
        if (shouldUpdateLatest) {
            novel.setLatestChapterId(saved.getId());
            novel.setLatestChapterTitle(saved.getTitle());
            novelRepository.save(novel);
        }
        return ResponseEntity.ok(saved);
    }

    @Data
    public static class UpsertChapterRequest {
        private String volume;
        private String title;
        private String content;
        private Integer wordCount;
    }

    private Chapter insertChapterWithSort(Novel novel, String volume, String chapterTitle, String htmlContent, int chapterWordCount) {
        long existingCount = chapterRepository.countByNovelId(novel.getId());
        Integer maxSortOrderSameVolume = chapterRepository.findMaxSortOrderByNovelIdAndVolume(novel.getId(), volume);
        Integer maxVolumeSortAll = chapterRepository.findMaxVolumeSortByNovelId(novel.getId());
        Integer volumeSortForVolume = chapterRepository.findVolumeSortByNovelIdAndVolume(novel.getId(), volume);

        Chapter chapter = new Chapter();
        chapter.setNovel(novel);
        chapter.setVolume(volume);
        chapter.setTitle(chapterTitle);
        chapter.setContent(htmlContent);
        chapter.setWordCount(chapterWordCount);

        if (existingCount == 0) {
            chapter.setVolumeSort(1);
            chapter.setSortOrder(0);
        } else if (maxSortOrderSameVolume != null && maxSortOrderSameVolume >= 0) {
            chapter.setVolumeSort(volumeSortForVolume != null && volumeSortForVolume > 0 ? volumeSortForVolume : 1);
            chapter.setSortOrder(maxSortOrderSameVolume + 1);
        } else {
            int nextSort = (maxVolumeSortAll != null ? maxVolumeSortAll : 0) + 1;
            chapter.setVolumeSort(nextSort);
            chapter.setSortOrder(0);
        }
        return chapterRepository.save(chapter);
    }

    private static int countNonWhitespaceChars(String raw) {
        if (raw == null || raw.isEmpty()) return 0;
        return raw.replaceAll("\\s+", "").length();
    }

    private static String buildChapterHtml(String raw) {
        if (raw == null || raw.isBlank()) return "";
        String[] lines = raw.replace("\r\n", "\n").split("\n");
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                sb.append("<br>");
                continue;
            }
            // 若用户已手动输入 <br>（或 <br/>/<br />），则保持为换行，不再包裹 <p>，也不重复追加
            if (trimmed.matches("(?i)^<br\\s*/?>$")) {
                sb.append("<br>");
                continue;
            }
            // 若用户粘贴了完整 HTML 片段（例如 <p> 或 <img>），则原样拼入（避免重复包裹）
            if (trimmed.matches("(?i)^<\\s*(p|img|br)\\b[\\s\\S]*")) {
                sb.append(trimmed);
                continue;
            }
            // 判断是否为图片路径：
            // 以 img/、/img/ 或 http(s) 开头，且以常见图片后缀结尾（允许文件名中包含空格）
            boolean looksLikeImage = looksLikeImagePath(trimmed);
            if (looksLikeImage) {
                String imgAlt = extractImageAlt(trimmed);
                sb.append("<img src=\"")
                        .append(escapeHtml(trimmed))
                        .append("\" alt=\"")
                        .append(escapeHtml(imgAlt))
                        .append("\" class=\"read-content-img\" />");
            } else {
                sb.append("<p class=\"read-dialogue\">")
                        .append(escapeHtml(trimmed))
                        .append("</p>");
            }
        }
        return sb.toString();
    }

    private static boolean looksLikeImagePath(String value) {
        if (value == null) return false;
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return false;
        String normalized = trimmed.replace("\\", "/");
        // 更宽松的识别：支持 http(s)、/img、img、以及带路径分隔符的相对路径
        // 同时必须以常见图片后缀结尾，避免把普通句子里带 ".jpg" 的情况误判
        return normalized.matches("(?i)^(https?://|/img/|img/|.+/).+\\.(jpg|jpeg|png|gif|webp)$");
    }

    private static String extractImageAlt(String imagePath) {
        if (imagePath == null || imagePath.isBlank()) return "图片";
        // 从路径末尾提取文件名（去掉扩展名），作为“简称/alt”
        // 例：/img/xxx_001.jpg -> xxx_001
        String normalized = imagePath.replace("\\", "/");
        int slashIdx = normalized.lastIndexOf('/');
        String fileName = slashIdx >= 0 ? normalized.substring(slashIdx + 1) : normalized;
        int dotIdx = fileName.lastIndexOf('.');
        return dotIdx > 0 ? fileName.substring(0, dotIdx) : fileName;
    }

    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
